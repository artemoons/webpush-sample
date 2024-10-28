const subscribeButton = document.getElementById('subscribeButton');
const unsubscribeButton = document.getElementById('unsubscribeButton');
const sendButton = document.getElementById('sendButton');
const sendInput = document.getElementById('sendInput');

if ("serviceWorker" in navigator) {
    try {
        checkSubscription();
        init();
    } catch (e) {
        console.error('Initialization error: ' + e);
    }

    subscribeButton.addEventListener('click', () => {
        subscribe().catch(e => {
            if (Notification.permission === 'denied') {
                console.warn('Permission for notifications was denied');
            } else {
                console.error('Subscription error: ' + e);
            }
        });
    });

    unsubscribeButton.addEventListener('click', () => {
        unsubscribe().catch(e => console.error('Unsubscription error: ' + e));
    });

    sendButton.addEventListener('click', () => {
        sendMessage().catch(e => console.error('Send message error: ' + e));
    });
}


async function checkSubscription() {
    const registration = await navigator.serviceWorker.ready;
    const subscription = await registration.pushManager.getSubscription();
    console.info('Checking subscription information...');
    if (subscription) {
        const response = await fetch("/api/v1/isSubscribed", {
            method: 'POST',
            body: JSON.stringify({endpoint: subscription.endpoint}),
            headers: {
                "content-type": "application/json"
            }
        });
        const subscribed = await response.json();

        if (subscribed) {
            subscribeButton.disabled = true;
            unsubscribeButton.disabled = false;
            sendInput.disabled = false;
            sendButton.disabled = false;
        }
        return subscribed;
    }
    return false;
}

async function init() {
    fetch('/api/v1/publicSigningKey')
        .then(response => response.arrayBuffer())
        .then(key => this.publicSigningKey = key)
        .finally(() => console.info('Application Server Public Key fetched from the server'));

    await navigator.serviceWorker.register("/sw.js", {
        scope: "/"
    });

    await navigator.serviceWorker.ready;
    console.info('Service Worker has been installed and is ready');
}

async function unsubscribe() {
    const registration = await navigator.serviceWorker.ready;
    const subscription = await registration.pushManager.getSubscription();
    if (subscription) {
        const successful = await subscription.unsubscribe();
        if (successful) {
            console.info('Unsubscription successful');

            await fetch("/api/v1/unsubscribe", {
                method: 'POST',
                body: JSON.stringify({endpoint: subscription.endpoint}),
                headers: {
                    "content-type": "application/json"
                }
            });

            console.info('Unsubscription info sent to the server');

            subscribeButton.disabled = false;
            unsubscribeButton.disabled = true;
            sendInput.disabled = true;
            sendButton.disabled = true;
        } else {
            console.error('Unsubscription failed');
        }
    }
}

async function subscribe() {
    const registration = await navigator.serviceWorker.ready;
    const subscription = await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: this.publicSigningKey
    });

    await fetch("/api/v1/subscribe", {
        method: 'POST',
        body: JSON.stringify(subscription),
        headers: {
            "content-type": "application/json"
        }
    });

    console.info(`Subscribed to Push Service: ${subscription.endpoint}`);
    console.info('Subscription info sent to the server');

    subscribeButton.disabled = true;
    unsubscribeButton.disabled = false;
    sendButton.disabled = false;
    sendInput.disabled = false;
}

async function sendMessage() {
    const inputText = {
        title: 'Push from webpage',
        body: sendInput.value
    };

    await fetch("/api/v1/send", {
        method: 'POST',
        body: JSON.stringify(inputText),
        headers: {
            "content-type": "application/json"
        }
    });
    console.info('Sent', inputText);
}