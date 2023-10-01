const PAC = "com.ayrlin.tasukaru";
const SRV = "command";

var i = { user: event.sender }

setIdentity(i);
checkWatchtime();

function setIdentity(i) {
    Plugins.callServiceMethod(PAC, SRV, "setIdentity", [JSON.stringify(i)]);
}

function checkWatchtime() {
    var wt = Plugins.callServiceMethod(PAC, SRV, "checkWatchtime", []);
    wt = msToReadableTime(wt);
    Koi.sendChat(event.streamer.platform, event.sender.displayname + " watchtime: " + wt, "SYSTEM", event.id);
}

function msToReadableTime(ms) {
    const seconds = Math.floor((ms / 1000) % 60);
    const minutes = Math.floor((ms / (1000 * 60)) % 60);
    const hours = Math.floor((ms / (1000 * 60 * 60)) % 24);
    const days = Math.floor(ms / (1000 * 60 * 60 * 24));

    const daysStr = days > 0 ? `${days}d ` : '';
    const hoursStr = hours > 0 ? `${hours}h ` : '';
    const minutesStr = minutes > 0 ? `${minutes}m ` : '';
    const secondsStr = `${seconds}s`;

    return `${daysStr}${hoursStr}${minutesStr}${secondsStr}`;
}
