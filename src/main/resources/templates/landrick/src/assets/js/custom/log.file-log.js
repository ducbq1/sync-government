const dateInput = $("#logDate");
const logContent = $('#log-content');
let eventSource;

$(document).ready(function () {
    getCurrentDate().then(x => dateInput.val(x)).then(r => initEventSource());
})

// Handle input change
dateInput.on("change", function () {
    const logDate = this.value;
    updateStreamDate(logDate).then(r => {
        setTimeout(x => {
            reactive();
        }, 500)
    }).then(r => initEventSource());
})

function initEventSource() {
    eventSource = new EventSource(`/api/log-management/logs`);

    // Clear existing content
    logContent.empty();

    eventSource.onopen = function (e) {
        console.log("Connection is open");
    };

    // Add the data
    eventSource.onmessage = function(event) {
        console.log('Received message:', event.data);
        logContent.prepend(event.data + '\n');
    };

    // Handle the error
    eventSource.onerror = function(event) {
        console.error('Error:', event);
    };
}

function clearLog() {
    logContent.empty();
    dateInput.val("");
}

// Helper function to update stream date
async function updateStreamDate(date) {
    const response = await interact(`/api/log-management/${date}`, {
        method: 'POST'
    })
    if (response) {
        return response;
    }
}

async function reactive() {
    const response = await interact(`/api/log-management/reactive`, {
        method: 'POST'
    })
    if (response) {
        return response;
    }
}

async function getCurrentDate() {
    const response = await interact(`/api/log-management/current-date`, {
        method: 'GET'
    })
    if (response) {
        return response;
    }
}