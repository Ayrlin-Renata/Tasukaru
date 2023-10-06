const PAC = "com.ayrlin.tasukaru";
const COMSRV = "command";
const SECSRV = "security";

var i = { user: event.sender }

if(!args[0]) {
    setIdentity(i, COMSRV);
    checkPoints();
} else {
    if(args[0] == "give") {
        var numPoints = 0;
        if(args[1] && !isNaN(parseInt(args[1]))) {
            numPoints = args[1];
        } else {
            //error
            return;
        }
        var recipiant = "";
        if(args[2]) {
            recipiant = args[2];
        } else {
            //error
            return;
        }
        var target = { 
            username: recipiant,
            platform: event.platform 
        }
        setIdentity(i, SECSRV);
        if(isMod()) {
            setIdentity(target, COMSRV);
            addPoints(numPoints);
        }
    }
}

function setIdentity(i, service) {
    Plugins.callServiceMethod(PAC, service, "setIdentity", [JSON.stringify(i)]);
}

function isMod() {
    var mod = Plugins.callServiceMethod(PAC, SECSRV, "isMod", [event.platform.name()]);
    return mod;
}

function checkPoints() {
    var points = Plugins.callServiceMethod(PAC, COMSRV, "checkPoints", []);
    Koi.sendChat(event.streamer.platform, event.sender.displayname + " points: " + points, "SYSTEM", event.id);
}

function addPoints(points) {
    Plugins.callServiceMethod(PAC, COMSRV, "addPoints", [points]);
    checkPoints();
}

