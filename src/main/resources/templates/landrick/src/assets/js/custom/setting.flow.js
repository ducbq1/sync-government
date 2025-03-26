let sortColumn = "";
let sortType = "";
let lstId = [];
let currentPage = 1;
let pageSize = 10;
const Header = {
    "checkbox": "",
    "name": "Name",
    "description": "Description",
    "sourceName": "Source Name",
    "destinationName": "Destination Name",
    "isActivated": "Status - Activated",
    "frequency": "Frequency",
    "progress": "Progress",
    "action": "Action"
};
const type = $("#type-container").attr("data-type");

$(document).ready(function () {
    // Get all list of category
    $("#resume").hide();
    startEvent();
    getAllSyncFlow(currentPage, pageSize).then(response => {
        init(parseJSON(response));
    });

    $("#changeStatusModal").on('hide.bs.modal', closeChangeFlowModal)
})

// init helper function
function init(response) {
    const tableDOM = createCategoryDOM(response);
    $("#category-table").html(tableDOM[0].prop("outerHTML") + tableDOM[1].prop("outerHTML"));

    // Initialize elements and event handlers
    checkboxInit();
    sortingInit(response);
    paginationInit(response);
    selectionConfigInit(response);
}

// Helper function to create DOM
function createCategoryDOM(response) {
    const tableDOM = [];
    const keys = Object.keys(Header);
    const tableHead = $("<thead></thead>");
    const tableHeadRow = $("<tr></tr>");
    const tableBody = $("<tbody></tbody>");

    // Add table head data
    for (let i = 0; i < keys.length; i++) {
        // Create Table Head
        if (i === 0) {
            // Create checkbox heading for table head
            const th = $("<th></th>").addClass("p-3 border-bottom").css("width", "50px");
            const formCheckHead = $("<div></div>").addClass("form-check");
            const selectAllCheckBox = $("<input>").attr({
                type: "checkbox",
                id: "select-all-checkbox"
            }).addClass("form-check-input border-dark");
            formCheckHead.append(selectAllCheckBox);
            th.append(formCheckHead);
            tableHeadRow.append(th);
        } else {
            // Create other columns for table heading
            const th = $("<th></th>").addClass("text-center border-bottom p-3")
                .css("min-width", "100px");
            // Create a text span
            const textSpan = $("<span></span>")
                .text(Header[keys[i]])
                .attr("id", keys[i])
            // Except for the last header
            if (i !== keys.length - 1) {
                textSpan.addClass("group-header fw-bold").css("cursor", "pointer")
            }

            // Set data-sort
            if (sortColumn && sortColumn === textSpan.attr("id")) {
                if (sortType && sortType !== "") {
                    textSpan.attr("data-sort", sortType);
                    const span = $("<span></span>")
                    // Add icon
                    if (sortType === 'asc') {
                        span.addClass("mdi mdi-sort-ascending position-absolute");
                    } else {
                        span.addClass("mdi mdi-sort-descending position-absolute");
                    }
                    th.append(textSpan);
                    th.append(span);
                } else {
                    textSpan.attr("data-sort", "");
                    th.append(textSpan);
                }
            } else {
                textSpan.attr("data-sort", "");
                th.append(textSpan);
            }
            tableHeadRow.append(th);
        }
    }

    // Tạo row cho body
    // Lấy data từ list danh mục
    for (let i = 0; i < response["lstFlow"].length; i++) {
        const tableBodyRow = $("<tr></tr>");
        for (let j = 0; j < 9; j++) {
            if (j === 0) {
                // Create checkbox heading
                const td = $("<td></td>").addClass("p-3").css("width", "50px");
                const formCheck = $("<div></div>").addClass("form-check");
                const checkBox = $("<input>").attr({
                    type: "checkbox",
                    "data-id": response['lstFlow'][i]['id'],
                    "data-status": response['lstFlow'][i]['isActivated']
                }).addClass("form-check-input border-dark");
                formCheck.append(checkBox);
                td.append(formCheck);
                tableBodyRow.append(td);
            } else {
                // Tạo row cho body
                // Lấy data từ list danh mục
                const td = $("<td></td>").addClass("text-center p-3 fw-semibold").css({
                    "min-width": "100px",
                    "font-size": "14px"
                });
                // Set title
                if (keys[j] === "isActivated") {
                    const div = $("<div></div>");
                    // Set title based on key
                    div.addClass(`badge ${response["lstFlow"][i][keys[j]] ? 'bg-soft-success' : 'bg-soft-danger'} rounded px-3 py-1`)
                        .text(`${response["lstFlow"][i][keys[j]] ? 'Active' : 'Inactive'}`);

                    td.append(div);
                } else if (keys[j] === "progress") {
                    const div = $(`
                    <div class="progress">
                        <div class="progress-bar" id="progress-bar-${response["lstFlow"][i]['id']}" role="progressbar" style="width: 0%;" aria-valuenow="0"
                             aria-valuemin="0" aria-valuemax="100">0%
                        </div>
                    </div>
                    `);

                    td.append(div);
                } else if (keys[j] === "frequency") {
                    const div = $(`
                    <div id="frequency-${response["lstFlow"][i]['id']}">
                    </div>
                    `);

                    td.append(div);
                } else if (keys[j] === "action") {
                    const div = $("<div></div>").addClass("d-flex align-items-center justify-content-center gap-1")
                    // Create 2 buttons
                    const editButton = $("<button></button>")
                        .addClass("border-0 bg-transparent")
                        .attr({
                            type: "button",
                            id: "editBtn",
                            "data-bs-toggle": "modal",
                            "data-bs-target": "#editFlowModal",
                            onclick: `${response["lstFlow"][i]["editButton"]["callback"]}(${response["lstFlow"][i]["id"]})`
                        })
                        .html(response["lstFlow"][i]["editButton"]["icon"])
                    // Add button to table
                    div.append(editButton);
                    td.append(div);
                } else {
                    td.text(response["lstFlow"][i][keys[j]]);
                }
                tableBodyRow.append(td);
            }
        }
        tableBody.append(tableBodyRow);
    }

    // Append to table
    tableHead.append(tableHeadRow);

    // Add to DOM list
    tableDOM.push(tableHead);
    tableDOM.push(tableBody);

    return tableDOM;
}

// Helper function to handle checkbox event
function checkboxInit() {
    const selectAllCheckBox = $("#select-all-checkbox");
    const multipleFlowsActivate = $("#multipleFlowsActivate");
    // Handle select all checkbox event
    selectAllCheckBox.click(function () {
        const checked = this.checked;
        $(".form-check > input[type='checkbox']").not("#flowIsActivated").not(multipleFlowsActivate).each(function () {
            this.checked = checked;
            const id = $(this).attr("data-id");
            const listOfChecked = $(".form-check > input[type='checkbox']:checked").not(selectAllCheckBox).not(multipleFlowsActivate).not("#flowIsActivated");
            if (checked) {
                if (!lstId.includes(id)) {
                    lstId = [...lstId, id];
                }
            } else {
                lstId = lstId.filter(id => id !== $(this).attr("data-id"));
            }
            // Hiện nút xóa all
            if (listOfChecked.length > 0) {
                const isAllActivated = Array.from(listOfChecked).every(x => $(x).attr("data-status") === "true");
                const isAllNotActivated = Array.from(listOfChecked).every(x => $(x).attr("data-status") === "false");
                if (isAllActivated) {
                    multipleFlowsActivate.removeClass('d-none').addClass("d-inline-block");
                    multipleFlowsActivate.prop("checked", true);
                } else if (isAllNotActivated) {
                    multipleFlowsActivate.removeClass('d-none').addClass("d-inline-block");
                    multipleFlowsActivate.prop("checked", false);
                } else {
                    multipleFlowsActivate.removeClass('d-inline-block').addClass('d-none');
                }
            } else {
                multipleFlowsActivate.removeClass('d-inline-block').addClass('d-none');
            }
        })
    });

    // Handle checkbox behavior
    $(".form-check > input[type='checkbox']").not("#select-all-checkbox").not("#flowIsActivated").not(multipleFlowsActivate).click(function () {
        const currentId = $(this).attr("data-id");
        const currentChecked = this.checked
        const listOfCheckbox = $(".form-check > input[type='checkbox']").not(selectAllCheckBox).not("#flowIsActivated").not(multipleFlowsActivate);
        const listOfChecked = $(".form-check > input[type='checkbox']:checked").not(selectAllCheckBox).not("#flowIsActivated").not(multipleFlowsActivate);
        // Check if the current checkbox is not checked
        if (!currentChecked) {
            lstId = lstId.filter(id => id !== currentId);
        } else {
            if (!lstId.includes(currentId)) {
                lstId = [...lstId, currentId];
            }
        }
        // Hiện nút xóa all
        if (listOfChecked.length > 0) {
            const isAllActivated = Array.from(listOfChecked).every(x => $(x).attr("data-status") === "true");
            const isAllNotActivated = Array.from(listOfChecked).every(x => $(x).attr("data-status") === "false");
            if (isAllActivated) {
                multipleFlowsActivate.removeClass('d-none').addClass("d-inline-block");
                multipleFlowsActivate.prop("checked", true);
            } else if (isAllNotActivated) {
                multipleFlowsActivate.removeClass('d-none').addClass("d-inline-block");
                multipleFlowsActivate.prop("checked", false);
            } else {
                multipleFlowsActivate.removeClass('d-inline-block').addClass('d-none');
            }
        } else {
            multipleFlowsActivate.removeClass('d-inline-block').addClass('d-none');
        }
        // Hiển thị trạng thái của checkbox all
        selectAllCheckBox.prop("checked", listOfCheckbox.length === listOfChecked.length);
    });
}

// Helper function to handle sorting event
function sortingInit(response) {
    let timeout;
    const groupHeader = $(".group-header");

    // Handle Sorting Event
    groupHeader.on('click', function (e) {
        clearTimeout(timeout);
        timeout = setTimeout(() => {
            // Clear data
            groupHeader.children("span").remove();

            // Handle sort
            sortColumn = $(this).attr("id");
            const order = $(this).attr("data-sort");
            const span = $("<span></span>")

            // Check order value
            if (order !== "") {
                if (order === "desc") {
                    sortType = "asc";
                    $(this).attr("data-sort", sortType);
                    span.addClass("mdi mdi-sort-ascending position-absolute");
                } else {
                    sortType = "desc";
                    $(this).attr("data-sort", sortType);
                    span.addClass("mdi mdi-sort-descending position-absolute");
                }
                $(this).append(span);
                // Reload data
                reloadFlow(2)
            } else {
                sortType = "desc";
                $(this).attr("data-sort", sortType);
                span.addClass("mdi mdi-sort-descending d-inline-block ms-1");
                $(this).append(span);
                // Reload data
                reloadFlow(2)
            }
        }, 150);

    }).on('dblclick', function () {
        clearTimeout(timeout);
        // Clear data
        $(this).attr("data-sort", "");
        sortColumn = "";
        sortType = "";
        // Get all list of category
        reloadFlow(1)
    })
}

// Helper function to handle pagination
function paginationInit(response) {
    const pagination = $("#pagination");
    // Clear pagination
    pagination.empty();
    // Handle Pagination
    const totalPages = Math.ceil(response["total"] / pageSize);
    const pageSizeElement = $("#pageSize");
    const start = totalPages !== 0 ? (currentPage - 1) * pageSize + 1 : 0;
    const end = Math.min(currentPage * pageSize, response["total"]);
    pageSizeElement.text(`Showing ${start} - ${end} out of ${response["total"]}`);

    // Create page link
    for (let i = 0; i < totalPages + 2; i++) {
        const li = $("<li></li>").addClass("page-item");
        const a = $("<a></a>").addClass("page-link");
        if (i === 0 || i === totalPages + 1) {
            a.attr({
                id: i === 0 ? "prev-button" : "next-button",
                href: "#",
                ariaLabel: i === 0 ? "Previous" : "Next"
            }).text(i === 0 ? "Prev" : "Next");
        } else {
            a.addClass('rounded-0')
            a.addClass("page-number").attr("href", "#").text(i);
            // Add active class to current page
            if (currentPage === parseInt(a.text())) {
                li.addClass("active")
            }
        }

        li.append(a);
        // Add to pagination
        pagination.append(li);
    }

    // Handle pagination event
    // Previous Button
    $("#prev-button").on("click", function () {
        if (currentPage - 1 >= 1) {
            paginationAPI(response, currentPage - 1);
        }
    })

    // Next Button
    $("#next-button").on("click", function () {
        if (currentPage + 1 <= totalPages) {
            paginationAPI(response, currentPage + 1);
        }
    })

    // Page button
    $(".page-number").on("click", function () {
        if (!$(this).parent().hasClass("active")) {
            paginationAPI(response, parseInt($(this).text()));
        }
    })
}

// Helper function to handle destination
function selectionConfigInit(response) {
    const flowSource = $("#flowSource");
    const flowDestination = $("#flowDestination");
    const newFlowSource = $("#newFlowSource");
    const newFlowDestination = $("#newFlowDestination");
    // Add Option
    flowSource.empty();
    flowSource.append('<option value="-1" selected>-- SELECT A SOURCE --</option>');
    newFlowSource.empty();
    newFlowSource.append('<option value="-1" selected>-- SELECT A SOURCE --</option>');
    flowDestination.empty();
    flowDestination.append('<option value="-1" selected>-- SELECT A DESTINATION --</option>');
    newFlowDestination.empty();
    newFlowDestination.append('<option value="-1" selected>-- SELECT A DESTINATION --</option>');
    for (const config of response["lstSourceConfig"]) {
        const newFlowOption = $("<option></option>").val(config.id).text(config.name);
        const flowOption = $("<option></option>").val(config.id).text(config.name);
        flowSource.append(flowOption);
        newFlowSource.append(newFlowOption)
    }

    for (const config of response["lstDestinationConfig"]) {
        const newFlowOption = $("<option></option>").val(config.id).text(config.name);
        const flowOption = $("<option></option>").val(config.id).text(config.name);
        flowDestination.append(flowOption)
        newFlowDestination.append(newFlowOption);
    }
}

// Helper function to call API on pagination
function paginationAPI(response, page) {
    currentPage = page;
    // Call API
    if (sortType !== "" && sortColumn !== "") {
        reloadFlow(2);
    } else {
        reloadFlow(1);
    }
}

// Handle change page size
function changePageSize(element) {
    // Clear data
    currentPage = 1;
    sortType = "";
    sortColumn = "";
    pageSize = parseInt(element.value);
    // Call API
    reloadFlow(1)
}

// Handle search
function searchFlow() {
    let searchUrl = `/api/flow/search?`
    // Get search value
    const flowName = $("#searchFlowName").val();
    const flowDescription = $("#searchFlowDescription").val();
    const flowSourceName = $("#searchFlowSource").val();
    const flowDestinationName = $("#searchFlowDestination").val();

    // Append search param
    if (flowName !== "") {
        searchUrl += `name=${flowName}&`
    }

    if (flowDescription !== "") {
        searchUrl += `description=${flowDescription}&`
    }

    if (flowSourceName !== "") {
        searchUrl += `sourceName=${flowSourceName}&`
    }

    if (flowDestinationName !== "") {
        searchUrl += `destinationName=${flowDestinationName}&`
    }

    // Remove the trailing '&' if there is one
    if (searchUrl.endsWith('&')) {
        searchUrl = searchUrl.slice(0, -1);
    }

    // Call API
    searchFlowAPI(searchUrl).then(response => {
        init(parseJSON(response));
    })
}

// Helper function to handle open edit modal
function openEditModal(id) {
    getSingleFlow(id).then(response => {
        // Get element
        const flowId = $("#flowId");
        const flowName = $("#newFlowName");
        const flowDescription = $("#newFlowDescription");
        const flowSource = $("#newFlowSource");
        const flowDestination = $("#newFlowDestination");
        const flowProxy = $("#newFlowProxy");
        const flowPayload = $("#newFlowPayload");
        const flowSavePath = $("#newFlowSavePath");
        const flowStatus = $("#flowIsActivated");
        const flowIsGetSyncedAgain = $("#flowIsGetSyncedAgain");

        // Set data
        flowId.val(response["id"]);
        flowName.val(response["name"]);
        flowSource.val(response["sourceId"]);
        flowDestination.val(response["destinationId"]);
        flowDescription.val(response["description"]);
        flowProxy.val(response["proxy"]);
        flowPayload.val(response["payload"]);
        flowSavePath.val(response["saveFilePath"]);
        flowStatus.prop("checked", response["isActivated"]);
        flowIsGetSyncedAgain.prop("checked", response["isGetSyncedAgain"]);
    })
}

// Handle save new or update flow
function saveOrUpdateFlow(type) {
    // Get Element
    const flowId = $("#flowId");
    const flowName = type === 1 ? $("#flowName") : $("#newFlowName");
    const flowDescription = type === 1 ? $("#flowDescription") : $("#newFlowDescription");
    const flowSource = type === 1 ? $("#flowSource") : $("#newFlowSource");
    const flowDestination = type === 1 ? $("#flowDestination") : $("#newFlowDestination");
    const flowProxy = type === 1 ? $("#flowProxy") : $("#newFlowProxy");
    const flowPayload = type === 1 ? $("#flowPayload") : $("#newFlowPayload");
    const flowSavePath = type === 1 ? $("#flowSavePath") : $("#newFlowSavePath");
    const isActivated = type === 1 ? true : $("#flowIsActivated").is(":checked");
    const isGetSyncedAgain = type === 1 ? true : $("#flowIsGetSyncedAgain").is(":checked");

    // Validate field
    if (!flowName.val() || flowName.val() === "") {
        toastr[Status.Waring]("Tên luồng không được để trống", "Cảnh báo");
        flowName.focus();
        return;
    }

    if (flowSource.val() === "-1") {
        toastr[Status.Waring]("Nguồn không được để trống", "Cảnh báo");
        flowSource.focus();
        return;
    }

    if (flowDestination.val() === "-1") {
        toastr[Status.Waring]("Đích không được để trống", "Cảnh báo");
        flowDestination.focus();
        return;
    }

    // Create object
    const syncFlowStaticObject = {
        id: type === 1 ? null : flowId.val(),
        name: flowName.val(),
        description: flowDescription.val(),
        createdBy: null,
        createdAt: null,
        updatedBy: null,
        updatedAt: null,
        isDeleted: null,
        sourceId: flowSource.val(),
        proxy: flowProxy.val(),
        payload: flowPayload.val(),
        saveFilePath: flowSavePath.val(),
        destinationId: flowDestination.val(),
        isActivated,
        isGetSyncedAgain
    }

    // Call API
    insertOrUpdateFlow(syncFlowStaticObject, type).then(response => {
        // Close modal
        if (type === 1) {
            $("#addFlowModal .close").click()
        } else if (type === 2) {
            $("#editFlowModal .close").click()
        }

        // Reload Category
        if (sortType !== "" && sortColumn !== "") {
            setTimeout(() => {
                reloadFlow(2)
            }, 300);
        } else {
            setTimeout(() => {
                reloadFlow(1)
            }, 300);
        }
    })
}

// Handle change flow status
function changeFlow() {
    // Get Id
    let flowId = lstId.join(',');
    const sourceStatus = $("#multipleFlowsActivate").is(":checked");
    // Call API
    changeFlowStatus(flowId, sourceStatus).then(response => {
        // Close modal
        $("#changeStatusModal .close").click()
        $("#multipleFlowsActivate").removeClass('d-inline-block').addClass('d-none');

        // Reload Category
        if (sortType !== "" && sortColumn !== "") {
            setTimeout(() => {
                reloadFlow(2)
            }, 300);
        } else {
            setTimeout(() => {
                reloadFlow(1)
            }, 300);
        }
    })
}

// Helper function to reload the flow page
// reloadType = 1 - getAllSyncFlow
// reloadType = 2 - getFlowOrderBy
function reloadFlow(reloadType) {
    // Add loader
    document.getElementById('preloader-table').style.visibility = 'visible';
    document.getElementById('preloader-table').style.opacity = '1';
    // Call API
    if (reloadType === 1) {
        getAllSyncFlow(currentPage, pageSize).then(response => {
            init(parseJSON(response));
        }).finally(() => {
            setTimeout(() => {
                document.getElementById('preloader-table').style.opacity = '0';
                document.getElementById('preloader-table').style.visibility = 'hidden';
            }, 350)
        })
    } else {
        getFlowOrderBy(sortColumn, sortType, currentPage, pageSize).then(response => {
            init(parseJSON(response));
        }).finally(() => {
            setTimeout(() => {
                document.getElementById('preloader-table').style.opacity = '0';
                document.getElementById('preloader-table').style.visibility = 'hidden';
            }, 350)
        })
    }
}

// Helper function clear modal form data
function clearModal() {
    $("#flowName").val("");
    $("#flowSource").val("-1");
    $("#flowDestination").val("-1");
    $("#flowDescription").val("");
    $("#settingActivate").checked = false;
}

// Helper function close change flow modal
function closeChangeFlowModal() {
    const changeStatusCheckbox = $("#multipleFlowsActivate");
    const isChecked = changeStatusCheckbox.prop("checked");
    changeStatusCheckbox.prop("checked", !isChecked);
}

// Helper function to parse JSON data
function parseJSON(response) {
    if (response["lstFlow"] && response["lstFlow"].length > 0) {
        response["lstFlow"] = response["lstFlow"].map(item => JSON.parse(item));
    }
    if (response["lstSourceConfig"] && response["lstSourceConfig"].length > 0) {
        response["lstSourceConfig"] = response["lstSourceConfig"].map(item => JSON.parse(item));
    }
    if (response["lstDestinationConfig"] && response["lstDestinationConfig"].length > 0) {
        response["lstDestinationConfig"] = response["lstDestinationConfig"].map(item => JSON.parse(item));
    }
    return response;
}

// Helper function to fetch list of sync flow
async function getAllSyncFlow(page, pageSize) {
    const response = await interact(`/api/flow?page=${page}&pageSize=${pageSize}`);
    if (response) {
        return response;
    }
}

// Helper function to fetch a sync flow
async function getSingleFlow(id) {
    const response = await interact(`/api/flow/${id}`);
    if (response) {
        return response;
    }
}

// Helper function to fetch list of flow by order
async function getFlowOrderBy(sortColumn, sortType, page, pageSize) {
    const response = await interact(`/api/flow?sortColumn=${sortColumn}&sortType=${sortType}&page=${page}&pageSize=${pageSize}`);
    if (response) {
        return response;
    }
}

// Helper function to search flow
async function searchFlowAPI(url) {
    const response = await interact(url);
    if (response) {
        return response;
    }
}

// Helper function to fetch list of flow
async function insertOrUpdateFlow(syncFlowObject, type) {
    const url = type === 1 ? 'create' : `${syncFlowObject.id}`;
    const method = type === 1 ? 'POST' : 'PUT';
    const body = JSON.stringify(syncFlowObject);
    const response = await interact(`/api/flow/${url}`, {
        method,
        headers: {
            'Content-Type': 'application/json'
        },
        body
    })
    if (response) {
        return response;
    }
}

// Helper function to change flow status
async function changeFlowStatus(id, status) {
    const response = await interact(`/api/flow/updateMany?flowID=${id}&status=${status}`, {
        method: 'PUT'
    })
    if (response) {
        return response;
    }
}

async function pause() {
    $("#start").hide();
    $("#resume").show();
    await interact('/api/scheduler/pause', {
        method: 'POST',
    });
}

async function resume() {
    $("#start").hide();
    await interact('/api/scheduler/resume', {
        method: 'POST',
    });
}

async function stop() {
    $("#start").show();
    $("#resume").hide();
    await interact('/api/scheduler/stop', {
        method: 'POST',
    });
}

async function start() {
    $("#start").hide();
    $("#resume").hide();
    await interact('/api/scheduler/start', {
        method: 'POST',
    });
    startEvent();
}

let eventSource;

function startEvent() {

    if (!window.EventSource) {
        toastr["error"]("Cảnh báo", "Your browser does not support EventSource")
        return;
    }

    eventSource = new EventSource("/api/scheduler/events");

    eventSource.onopen = function (e) {
        console.log("Connection is open");
    };

    eventSource.onerror = function (e) {
        if (this.readyState == EventSource.CONNECTING) {
            console.log("Connection is interrupted, connecting ...");
        } else {
            console.log("Error, state: " + this.readyState);
        }
    };

    eventSource.onmessage = function (e) {
        console.log(e);
        if (e == null) stopEvent();
        let dataJson = JSON.parse(e.data);
        Object.keys(dataJson).forEach(key => {
            let value = Math.ceil(dataJson[key] % 100);
            let frequency = Math.floor(dataJson[key] / 100) + 1;
            if ($(`#progress-bar-${key}`)) {
                $(`#progress-bar-${key}`).css("width", `${value}%`);
                $(`#progress-bar-${key}`).text(`${value}%`);
            }
            if ($(`#frequency-${key}`)) {
                $(`#frequency-${key}`).text(`${frequency}`);
            }
        })
        // if (e == "-1") {
        //     stopEvent();
        // } else {
        //     if ($("#sync-progress")) {
        //         $("#sync-progress").attr("aria-valuenow", e)
        //         $("#sync-progress").css("width", `${e}%`);
        //         $("#sync-progress").text(`${e}%`);
        //     }
        // }
    };
}

function stopEvent() {
    eventSource.close();
    console.log("Connection is closed");
}