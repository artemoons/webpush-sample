self.addEventListener('activate', event => event.waitUntil(clients.claim()));

self.addEventListener('push', event => event.waitUntil(handlePushEvent(event)));

self.addEventListener('notificationclick', event => event.waitUntil(handleNotificationClick(event)));

self.addEventListener('notificationclose', event => console.info('notificationclose event fired'));

async function handlePushEvent(event) {
    console.info('push event emitted');

    const needToShow = await needToShowNotification();

    console.info('Received new message');

    const msg = event.data.json();

    if (needToShow) {
        self.registration.showNotification(msg.title, {
            body: msg.body,
            icon: 'icons8-message-96.png'
        });
    }
}

const allClients = await clients.matchAll({includeUncontrolled: true});
for (const client of allClients) {
    client.postMessage('data-updated');
}

const urlToOpen1 = new URL('/index.html', self.location.origin).href;
const urlToOpen2 = new URL('/', self.location.origin).href;

async function handleNotificationClick(event) {

    let openClient = null;
    const allClients = await clients.matchAll({includeUncontrolled: true, type: 'window'});
    for (const client of allClients) {
        if (client.url === urlToOpen1 || client.url === urlToOpen2) {
            openClient = client;
            break;
        }
    }

    if (openClient) {
        await openClient.focus();
    } else {
        await clients.openWindow(urlToOpen1);
    }

    event.notification.close();
}

async function needToShowNotification() {
    const allClients = await clients.matchAll({includeUncontrolled: true});
    for (const client of allClients) {
        if (client.visibilityState === 'visible') {
            return true;
        }
    }
    return true;
}
