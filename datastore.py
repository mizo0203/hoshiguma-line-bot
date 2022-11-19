from google.cloud import datastore

# For help authenticating your client, visit
# https://cloud.google.com/docs/authentication/getting-started
client = datastore.Client()


def save(key, value):
    task = datastore.Entity(key=client.key("Task", key))
    task.update({"value": value})
    client.put(task)


def load(key, value):
    try:
        return client.get(client.key("Task", key))["value"]
    except TypeError:
        save(key, value)
        return value
