const PAC = "com.ayrlin.tasukaru";
const SRV = "interop";

var i = { user: event.sender }

setIdentity(i);
checkPoints();

function setIdentity(i) {
    Plugins.callServiceMethod(PAC, SRV, "setIdentity", [JSON.stringify(i)]);
}

function checkPoints() {
    var points = Plugins.callServiceMethod(PAC, SRV, "checkPoints", []);
    Koi.sendChat(event.streamer.platform, event.sender.displayname + " points: " + points, "SYSTEM", event.id);
}