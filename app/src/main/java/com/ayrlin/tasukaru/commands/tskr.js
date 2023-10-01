const PAC = "com.ayrlin.tasukaru";
const SRV = "interop";

var i = new Object();
i.user = event.sender;

checkPoints(i);

function checkPoints(i) {
    var points = Plugins.callServiceMethod(PAC, SRV, "checkPoints", [i]);
    Koi.sendChat(event.streamer.platform, "Points: " + points, event.id);
}