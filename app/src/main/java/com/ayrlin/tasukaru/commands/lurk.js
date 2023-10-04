const PAC = "com.ayrlin.tasukaru";
const SRV = "command";

var i = { user: event.sender }

setIdentity(i);
setLurk();

function setIdentity(i) {
    Plugins.callServiceMethod(PAC, SRV, "setIdentity", [JSON.stringify(i)]);
}

function setLurk() {
    Plugins.callServiceMethod(PAC, SRV, "setLurk", [true]);
    Koi.sendChat(event.streamer.platform, "Pro lurker " + event.sender.displayname + " collectin' extra passive channel points with !lurk", "SYSTEM", event.id);
}