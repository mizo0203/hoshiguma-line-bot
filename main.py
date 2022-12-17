# Copyright 2022 P Hackathon
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import urllib

from flask import Flask, redirect, render_template, request, url_for

# [START gae_python38_datastore_store_and_fetch_times]
# [START gae_python3_datastore_store_and_fetch_times]
from google.cloud import datastore

import datastore as store
import line

# from create_app_engine_queue_task import get_day_of_first_wed
from sub import (
    as_tokyo_time,
    create_task_2,
    get_event_list_def,
    get_new_event,
    square,
    square2,
    square3,
    today,
    votes,
)

# from google.appengine.api import app_identity

datastore_client = datastore.Client()

# [END gae_python3_datastore_store_and_fetch_times]
# [END gae_python38_datastore_store_and_fetch_times]
app = Flask(__name__)
app.jinja_env.globals["url_encode"] = lambda d: urllib.parse.quote(str(dict(d)))
app.jinja_env.globals["as_tokyo_time"] = lambda d: as_tokyo_time(d)


@app.route("/")
def root():
    participants = store.load("participants", [])
    candidate_dates: list = store.load("candidate_dates", [])
    event_list = store.load("event_list", get_event_list_def())
    return render_template(
        "index.html",
        participants=participants,
        votes=votes(participants),
        candidate_dates=list(map(as_tokyo_time, candidate_dates)),
        event=event_list[-1],
    )


@app.route("/events.ics")
def events():
    event_list = store.load("event_list", get_event_list_def())
    print(event_list)
    return render_template("events.ics", events=map(square, event_list))


@app.route("/table", methods=["POST"])
def table():
    participants = store.load("participants", [])
    candidate_dates: list = store.load("candidate_dates", [])
    if not candidate_dates:
        return redirect(url_for("root"))  # TODO: エラー処理
    participant_new = square2(request.form)
    for i, participant in enumerate(participants):
        if participant["name"] == participant_new["name"]:
            participants[i] = participant_new
            break
    else:
        participants.append(participant_new)
    store.save("participants", participants)
    return redirect(url_for("root"))


@app.route("/apply_participation", methods=["POST"])
def apply_participation():
    candidate_dates: list = store.load("candidate_dates", [])
    event_list = store.load("event_list", get_event_list_def())
    if candidate_dates:
        return redirect(url_for("root"))  # TODO: エラー処理
    participant_new = square3(request.form)
    for i, participant in enumerate(event_list[-1]["participants"]):
        if participant["name"] == participant_new["name"]:
            event_list[-1]["participants"][i] = participant_new
            break
    else:
        event_list[-1]["participants"].append(participant_new)
    store.save("event_list", event_list)
    return redirect(url_for("root"))


@app.route("/cancel_participation", methods=["POST"])
def cancel_participation():
    candidate_dates: list = store.load("candidate_dates", [])
    event_list = store.load("event_list", get_event_list_def())
    if candidate_dates:
        return redirect(url_for("root"))  # TODO: エラー処理
    name = request.form["name"].strip()[:5]
    for i, participant in enumerate(event_list[-1]["participants"]):
        if participant["name"] == name:
            event_list[-1]["participants"].pop(i)
            break
    store.save("event_list", event_list)
    return redirect(url_for("root"))


@app.route("/_ah/warmup")
def warmup():
    # Handle your warmup logic here, e.g. set up a database connection pool
    return "", 200, {}


@app.route("/notify/schedule_approached", methods=["GET"])
def schedule_approached():
    """開催当日 15 分前"""
    notify_info = store.load(
        "notify_info",
        {
            "line_notify_access_token": "",
            "url": "",
            "zoom_url": "",
            "zoom_pass": "",
        },
    )
    return line.notify(notify_info["line_notify_access_token"], "開戦 15 分前です。")


@app.route("/notify/schedule_adjustment_started", methods=["GET"])
def schedule_adjustment_started():
    """開催当日 22:00 次回日程調整開始済みをお知らせ"""

    from sub import get_candidate_dates, get_day_of_first_wed

    first_wed_date = get_day_of_first_wed()
    participants = [
        {
            "name": "みぞ",
            "comment": "対戦よろしくお願いします",
            "available_days": [1, 1, 1, 1],
        }
    ]
    store.save("participants", participants)
    candidate_dates = get_candidate_dates(first_wed_date)
    store.save("candidate_dates", candidate_dates)
    print(f"Received task with payload: {candidate_dates}")

    notify_info = store.load(
        "notify_info",
        {
            "line_notify_access_token": "",
            "url": "",
            "zoom_url": "",
            "zoom_pass": "",
        },
    )
    return line.notify(
        notify_info["line_notify_access_token"],
        f"""\
対戦ありがとうございました！
早速ですが、次回の開催日希望調査を開始しました。
{today(get_day_of_first_wed())} 20:00 までに入力をよろしくお願いいたします。
{notify_info["url"]}\
    """,
    )


@app.route("/notify/schedule_adjustment_completion_date", methods=["GET"])
def schedule_adjustment_completion_date():
    """日程調整完了日を 11:45 にお知らせする"""
    notify_info = store.load(
        "notify_info",
        {
            "line_notify_access_token": "",
            "url": "",
            "zoom_url": "",
            "zoom_pass": "",
        },
    )
    return line.notify(
        notify_info["line_notify_access_token"],
        f"""\
開催日希望調査は
本日 {today()} 20:00 までです。
ご協力よろしくお願いいたします。
{notify_info["url"]}\
    """,
    )


@app.route("/notify/schedule_adjustment_finalizing", methods=["GET"])
def schedule_adjustment_finalizing():
    """
    20:00 に日程調整完了させ
    日程直前・直後のお知らせを準備する
    日程調整完了したことを 20:00 にお知らせする
    """
    participants = store.load("participants", [])
    candidate_dates: list = store.load("candidate_dates", [])
    event_list = store.load("event_list", get_event_list_def())
    new_event = get_new_event(participants, candidate_dates)
    create_task_2(
        new_event,
        store.load(
            "queue_path",
            {
                "project": "",
                "location": "",
                "queue": "",
            },
        ),
    )
    event_list.append(new_event)
    store.save("event_list", event_list)
    store.save("candidate_dates", [])
    notify_info = store.load(
        "notify_info",
        {
            "line_notify_access_token": "",
            "url": "",
            "zoom_url": "",
            "zoom_pass": "",
        },
    )
    "line_notify_access_token"
    return line.notify(
        notify_info["line_notify_access_token"],
        f"""\
次回の開催日時は
{today(event_list[-1]["timestamp"])} 20:00 - 23:00 です。
よろしくお願いいたします。
{notify_info["url"]}
開催当日まで、参加者募集中です！

Zoom URL:
{notify_info["zoom_url"]}
パスコード: {notify_info["zoom_pass"]}\
    """,
    )


# [END cloud_tasks_appengine_quickstart]


if __name__ == "__main__":
    # This is used when running locally only. When deploying to Google App
    # Engine, a webserver process such as Gunicorn will serve the app. This
    # can be configured by adding an `entrypoint` to app.yaml.

    # Flask's development server will automatically serve static files in
    # the "static" directory. See:
    # http://flask.pocoo.org/docs/1.0/quickstart/#static-files. Once deployed,
    # App Engine itself will serve those files as configured in app.yaml.
    app.run(host="127.0.0.1", port=8080, debug=True)
