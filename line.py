import requests


def notify(access_token, message):
    return requests.post(
        url="https://notify-api.line.me/api/notify",
        params={"message": message},
        headers={
            "Content-Type": "application/x-www-form-urlencoded",
            "Authorization": f"Bearer {access_token}",
        },
    ).json()
