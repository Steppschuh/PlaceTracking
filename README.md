# PlaceTracking
Simple and free to use JSON API for time / location / whatever tracking.

[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/434707094bd26e283ffc)


## Example Usage
Follow [this guide](http://random-how-to.com/free-automated-time-tracking/) to learn how to set this up as automated time tracking solution.

### Time Tracking
The most obvious use case is logging how much time you've spent on a project:

1. Create a User ("Your Name")
2. Create a Topic ("Project Name")
3. Add an action each time you start working ("start")
4. Add an action each time you stop working ("stop")

You can use the collected data to analyse how much time you (or your team) spent on the project.

### Location Tracking
You can also use the API to track when you arrive at or leave a location (for example the gym or your office):

1. Create a User ("Your Name")
2. Create a Topic ("Location Name")
3. Add an action each time you arrive ("arrive")
4. Add an action each time you leave ("leave")

You can use the collected data to check who's currently at the office or when you've been at the gym recently.

### IFTTT Automatization
Of course you don't want to manually call this API every time. You can automatically trigger actions using [IFTTT](https://ifttt.com/). I'd suggest using the [Maker channel](https://ifttt.com/maker), it allows you to fire web requests to this API. You could use the [Location channel](https://ifttt.com/android_location) as a trigger when you arrive at or leave a location.


## Available API endpoints
All the API endpoints listed below are reachable at:
```
http://placetracking.appspot.com/api/
```
### Users
#### Adding a user
You can create users (no account required), simply by providing a **name**:
```
/api/users/add/?name=John%20Doe
```
Make sure that you store the **id** of the returned user object, you will need it later (referred as **userId**).

### Topics
#### Adding a topic
A topic is anything that you want to track, like time spent on a project or when you arrive at a place. You can create a new topic by providing a **name**:
```
/api/topics/add/?name=working
```
Make sure that you store the **id** of the returned topic object, you will need it later (referred as **topicId**).

### Actions
#### Adding an action
An action can bee seen as an event or indicator for a topic. You can create a new action by providing a **name**, your **userId** and a **topicId**:
```
/api/actions/add/?name=start&userId=5629499534213120&topicId=5707702298738688
```
In addition to these 3 parameters, the API will also store the current **timestamp**. It's up to you what you select as **name** for your action, but you should define some constants for you (or your team). Common action pairs could be:
* *start* | *stop*
* *pause* | *resume*
* *arrive* | *leave*

#### Deleting an action
If you messed up for some reason, you just need to pass the action **id** to this endpoint:
```
/api/actions/delete/?id=8797702308738690
```

#### Fetching actions
If you want to get a list of recent actions (which is the point of this API), you need to specify a **userId** or **topicId** (or both):
```
/api/actions/get/?userId=5629499534213120&topicId=5707702298738688
```
However, you can also combine them with some additional filters:
* **name**="start"
* **minimumTimestamp**=1468413570000
* **maximumTimestamp**=1468417170000
* **limit**=50
* **offset**=25
