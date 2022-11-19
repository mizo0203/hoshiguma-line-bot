import calendar
from datetime import datetime, timedelta, timezone

from dateutil import tz


def square(event):
    start: datetime = event["timestamp"].astimezone(tz.gettz("Asia/Tokyo"))
    end = start + timedelta(hours=3)
    return {
        "uid": start.strftime("%Y%m%dT%H%M00"),
        "summary": "Among Us",
        "description": "Among Us",
        "date": {
            "start": start.strftime("%Y%m%dT%H%M00"),
            "end": end.strftime("%Y%m%dT%H%M00"),
        },
    }


def square2(form):
    return {
        "name": form["name"].strip()[:5],
        "comment": form["comment"].strip()[:30],
        "available_days": [
            int(form["radio_0"]),
            int(form["radio_1"]),
            int(form["radio_2"]),
            int(form["radio_3"]),
        ],
    }


def square3(form):
    return {
        "name": form["name"].strip()[:5],
        "comment": form["comment"].strip()[:30],
    }


def as_tokyo_time(dt: datetime):
    return dt.astimezone(tz.gettz("Asia/Tokyo")).strftime("%m/%d")


def today(dt: datetime = datetime.now(tz=tz.gettz("Asia/Tokyo"))):
    return dt.strftime("%m/%d")


def create_task(dt, relative_uri, queue_path):
    import sys

    # [START cloud_tasks_appengine_create_task]
    """Create a task for a given queue with an arbitrary payload."""

    from google.cloud import tasks_v2
    from google.protobuf import timestamp_pb2

    # Create a client.
    client = tasks_v2.CloudTasksClient()

    # Construct the fully qualified queue name.
    parent = client.queue_path(
        queue_path["project"],
        queue_path["location"],
        queue_path["queue"],
    )

    # Create Timestamp protobuf.
    timestamp = timestamp_pb2.Timestamp()
    timestamp.FromDatetime(dt)

    # Construct the request body.
    # Add the timestamp to the tasks.
    task = {
        "app_engine_http_request": {  # Specify the type of request.
            "http_method": tasks_v2.HttpMethod.POST,
            "relative_uri": relative_uri,
        },
        "schedule_time": timestamp,
    }

    # Use the client to build and send the task.
    #
    try:
        response = client.create_task(parent=parent, task=task)
        print(f"Created task {response.name}")
    except Exception as e:
        print(f"Created task {e}", file=sys.stderr)


def create_task_2(new_event, queue_path):
    print(f"queue_path: {queue_path}")
    print(f"queue_path type: {type(queue_path)}")
    create_task(
        (new_event["timestamp"] - timedelta(minutes=15)),
        "/notify/schedule_approached",
        queue_path,
    )
    create_task(
        (new_event["timestamp"] + timedelta(hours=2)),
        "/notify/schedule_adjustment_starting",
        queue_path,
    )
    create_task(
        (new_event["timestamp"] + timedelta(hours=3)),
        "/notify/schedule_adjustment_started",
        queue_path,
    )


def votes(participants):
    votes = [[0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0]]
    for participant in participants:
        for i, available_day in enumerate(participant["available_days"]):
            votes[i][0] += available_day == 1
            votes[i][1] += available_day == 0
            votes[i][2] += available_day == -1
    return votes


def get_day_of_first_wed():
    tmp: datetime = datetime.now() + timedelta(days=28)
    first_dow, _ = calendar.monthrange(tmp.year, tmp.month)
    day = (2 - first_dow) % 7 + 1  # Wednesday(2)
    return datetime(tmp.year, tmp.month, day, 20, 00, tzinfo=tz.gettz("Asia/Tokyo"))


def get_candidate_dates(first_wed_date):
    result = []
    date = first_wed_date.astimezone(timezone.utc)
    for num in range(3, 25, 7):
        result.append(date + timedelta(days=num))
    return result


def get_event_list_def():
    return [
        {
            "timestamp": datetime(
                2022, 5, 7, 20, 00, tzinfo=tz.gettz("Asia/Tokyo")
            ).astimezone(timezone.utc),
            "participants": [
                {
                    "name": "みぞ",
                    "comment": "対戦よろしくお願いします",
                },
                {
                    "name": "NAME_A",
                    "comment": "ミゾを吊る。ただそれだけだ。",
                },
                {
                    "name": "NAME_B",
                    "comment": "",
                },
                {
                    "name": "NAME_C",
                    "comment": "初陣(─.─||）",
                },
                {
                    "name": "NAME_D",
                    "comment": "",
                },
                {
                    "name": "NAME_E",
                    "comment": "",
                },
                {
                    "name": "NAME_F",
                    "comment": "横浜に着いた。",
                },
            ],
        },
        {
            "timestamp": datetime(
                2022, 6, 18, 20, 00, tzinfo=tz.gettz("Asia/Tokyo")
            ).astimezone(timezone.utc),
            "participants": [
                {
                    "name": "みぞ",
                    "comment": "対戦よろしくお願いします",
                },
                {
                    "name": "NAME_A",
                    "comment": "みぞを狩れ！",
                },
                {
                    "name": "NAME_B",
                    "comment": "",
                },
                {
                    "name": "NAME_C",
                    "comment": "黒塗りはされない",
                },
                {
                    "name": "NAME_D",
                    "comment": "",
                },
                {
                    "name": "NAME_E",
                    "comment": "いまのところ",
                },
                {
                    "name": "NAME_F",
                    "comment": "",
                },
                {
                    "name": "NAME_G",
                    "comment": "",
                },
            ],
        },
    ]


def get_new_event(participants, candidate_dates):
    wait = len(participants) + 1
    print(f"wait: {wait}")
    max = (0, 0)
    for i, vote in enumerate(votes(participants)):
        tmp = (vote[0] * wait) + vote[1]
        print(f"{max} / {i} / {tmp} / {vote}")
        if max[1] < tmp:
            max = (i, tmp)
    return {
        "timestamp": candidate_dates[max[0]],
        "participants": list(
            map(
                lambda p: {"name": p["name"], "comment": p["comment"]},
                [p for p in participants if not p["available_days"][max[0]] == -1],
            )
        ),
    }
