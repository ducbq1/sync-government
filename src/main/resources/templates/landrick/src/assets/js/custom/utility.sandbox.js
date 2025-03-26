let sortColumn = "";
let sortType = "";
let lstId = [];
let currentPage = 1;
let pageSize = 10;
const Header = {
    "checkbox": "",
    "name": "Name",
    "description": "Description",
    "url": "URL",
    "isConnected": "Connected",
    "isActivated": "Activated",
    "action": "Action"
};
const type = $("#type-container").attr("data-type");

$(document).ready(function () {
    // Get all list of category

    fetch(`/api/utility/sandbox`).then(x => x.json()).then(x => init(x));

    // getAllConfig(currentPage, pageSize).then(response => {
    //     init(parseJSON(response));
    // });

    $("#changeStatusModal").on('hide.bs.modal', closeChangeConfigModal)
})

// init helper function
function init(response) {
    const tableDOM = createCategoryDOM(response);
    $("#category-table").html(tableDOM[0].prop("outerHTML") + tableDOM[1].prop("outerHTML"));

    // Initialize elements and event handlers
    checkboxInit();
    sortingInit(response);
    paginationInit(response);
}

// Helper function to create DOM
function createCategoryDOM(response) {
    const tableDOM = [];
    const keys = ["checkbox", ...Object.keys(response[0]), "action_temp"];
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
                .text(keys[i])
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
    for (let i = 0; i < response.length; i++) {
        const tableBodyRow = $("<tr></tr>");
        for (let j = 0; j < keys.length; j++) {
            if (j === 0) {
                // Create checkbox heading
                const td = $("<td></td>").addClass("p-3").css("width", "50px");
                const formCheck = $("<div></div>").addClass("form-check");
                const checkBox = $("<input>").attr({
                    type: "checkbox",
                    "data-id": response[i]['id'],
                    "data-status": response[i]['isActivated']
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
                    div.addClass(`badge ${response[i][keys[j]] ? 'bg-soft-success' : 'bg-soft-danger'} rounded px-3 py-1`)
                        .text(`${response[i][keys[j]] ? 'Active' : 'Inactive'}`);

                    td.append(div);
                } else if (keys[j] === "isConnected") {
                    const div = $("<div></div>");
                    // Set title based on key
                    div.addClass(`badge ${response[i][keys[j]] ? 'bg-soft-success' : 'bg-soft-danger'} rounded px-3 py-1`)
                        .text(`${response[i][keys[j]] ? 'Conneced' : 'Not Connected'}`);

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
                            "data-bs-target": "#editConfigModal",
                            onclick: `${response[i]["editButton"]["callback"]}(${response[i]["id"]})`
                        })
                        .html(response[i]["editButton"]["icon"])

                    const checkButton = $("<button></button>")
                        .addClass("border-0 bg-transparent")
                        .attr({
                            type: "button",
                            id: "checkBtn",
                            onclick: `${response[i]["checkButton"]["callback"]}(${response[i]["id"]})`
                        })
                        .html(response[i]["checkButton"]["icon"])
                    // Add button to table
                    div.append(editButton);
                    div.append(checkButton);
                    td.append(div);
                } else {
                    td.text(response[i][keys[j]]);
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
    const multipleConfigActivate = $("#multipleConfigActivate");
    // Handle select all checkbox event
    selectAllCheckBox.click(function () {
        const checked = this.checked;
        $(".form-check > input[type='checkbox']").not("#configIsActivated").not(multipleConfigActivate).each(function () {
            this.checked = checked;
            const id = $(this).attr("data-id");
            const listOfChecked = $(".form-check > input[type='checkbox']:checked").not(selectAllCheckBox).not(multipleConfigActivate).not("#configIsActivated");
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
                    multipleConfigActivate.removeClass('d-none').addClass("d-inline-block");
                    multipleConfigActivate.prop("checked", true);
                } else if (isAllNotActivated) {
                    multipleConfigActivate.removeClass('d-none').addClass("d-inline-block");
                    multipleConfigActivate.prop("checked", false);
                } else {
                    multipleConfigActivate.removeClass('d-inline-block').addClass('d-none');
                }
            } else {
                multipleConfigActivate.removeClass('d-inline-block').addClass('d-none');
            }
        })
    });

    // Handle checkbox behavior
    $(".form-check > input[type='checkbox']").not("#select-all-checkbox").not("#configIsActivated").not(multipleConfigActivate).click(function () {
        const currentId = $(this).attr("data-id");
        const currentChecked = this.checked;
        const listOfCheckbox = $(".form-check > input[type='checkbox']").not(selectAllCheckBox).not("#configIsActivated").not(multipleConfigActivate);
        const listOfChecked = $(".form-check > input[type='checkbox']:checked").not(selectAllCheckBox).not("#configIsActivated").not(multipleConfigActivate);
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
            const isAllActivated = Array.from(listOfChecked).every(x => x.attr("data-status"));
            const isAllNotActivated = Array.from(listOfChecked).every(x => !x.attr("data-status"));
            if (isAllActivated) {
                multipleConfigActivate.removeClass('d-none').addClass("d-inline-block");
                multipleConfigActivate.prop("checked", true);
            } else if (isAllNotActivated) {
                multipleConfigActivate.removeClass('d-none').addClass("d-inline-block");
                multipleConfigActivate.prop("checked", false);
            } else {
                multipleConfigActivate.removeClass('d-inline-block').addClass('d-none');
            }
        } else {
            multipleConfigActivate.removeClass('d-inline-block').addClass('d-none');
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
                reloadConfig(2)
            } else {
                sortType = "desc";
                $(this).attr("data-sort", sortType);
                span.addClass("mdi mdi-sort-descending d-inline-block ms-1");
                $(this).append(span);
                // Reload data
                reloadConfig(2)
            }
        }, 150);

    }).on('dblclick', function () {
        clearTimeout(timeout);
        // Clear data
        $(this).attr("data-sort", "");
        sortColumn = "";
        sortType = "";
        // Get all list of category
        reloadConfig(1)
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

// Helper function to call API on pagination
function paginationAPI(response, page) {
    currentPage = page;
    // Call API
    if (sortType !== "" && sortColumn !== "") {
        reloadConfig(2);
    } else {
        reloadConfig(1);
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
    reloadConfig(1)
}

// Handle search
function searchConfig() {
    let searchUrl = `/api/database-config/search?`
    // Get search value
    const configName = $("#searchConfigName").val();
    const configDescription = $("#searchConfigDescription").val();
    const configUrl = $("#searchConfigUrl").val();

    // Append search param
    if (configName !== "") {
        searchUrl += `name=${configName}&`
    }

    if (configDescription !== "") {
        searchUrl += `description=${configDescription}&`
    }

    if (configUrl !== "") {
        searchUrl += `url=${configUrl}&`
    }

    // Remove the trailing '&' if there is one
    if (searchUrl.endsWith('&')) {
        searchUrl = searchUrl.slice(0, -1);
    }

    // Call API
    searchConfigAPI(searchUrl).then(response => {
        init(parseJSON(response));
    })
}

// Helper function to handle open edit modal
function openEditModal(id) {
    getSingleConfig(id).then(response => {
        // Get element
        const configId = $("#configId");
        const configName = $("#newConfigName");
        const configDescription = $("#newConfigDescription");
        const configUrl = $("#newConfigUrl");
        const configUsername = $("#newConfigUsername");
        const configPassword = $("#newConfigPassword");
        const configDriver = $("#newConfigDriver");
        const configPort = $("#newConfigPort");
        const configService = $("#newConfigService");
        const sourceStatus = $("#configIsActivated");

        // Set data
        configId.val(response["id"]);
        configName.val(response["name"]);
        configDescription.val(response["description"]);
        configUrl.val(response["url"]);
        configUsername.val(response["userName"]);
        configPassword.val(response["password"]);
        configDriver.val(response["driver"]);
        configPort.val(response["port"]);
        configService.val(response["service"]);
        sourceStatus.prop("checked", response["isActivated"]);
    })
}

// Handle change config status
function checkConnection(id) {
    checkConnectionAPI(id).then(response => reloadConfig(1));
}

// Handle save new or update config
function saveOrUpdateConfig(type) {
    // Get Element
    const configId = $("#configId");
    const configName = type === 1 ? $("#configName") : $("#newConfigName");
    const configDescription = type === 1 ? $("#configDescription") : $("#newConfigDescription");
    const configUrl = type === 1 ? $("#configUrl") : $("#newConfigUrl");
    const configUsername = type === 1 ? $("#configUsername") : $("#newConfigUsername");
    const configPassword = type === 1 ? $("#configPassword") : $("#newConfigPassword");
    const configDriver = type === 1 ? $("#configDriver") : $("#newConfigDriver");
    const configPort = type === 1 ? $("#configPort") : $("#newConfigPort");
    const configService = type === 1 ? $("#configService") : $("#newConfigService");
    const isActivated = type === 1 ? true : $("#configIsActivated").is(":checked");

    // Validate field
    if (!configName.val() || configName.val() === "") {
        toastr[Status.Waring]("Tên cấu hình không được để trống", "Cảnh báo");
        configName.focus();
        return;
    }

    if (!configUrl.val() || configUrl.val() === "") {
        toastr[Status.Waring]("Đường dẫn không được để trống", "Cảnh báo");
        configUrl.focus();
        return;
    }

    if (!configUsername.val() || configUsername.val() === "") {
        toastr[Status.Waring]("Tên đăng nhập không được để trống", "Cảnh báo");
        configUsername.focus();
        return;
    }

    if (!configPassword.val() || configUsername.val() === "") {
        toastr[Status.Waring]("Mật khẩu không được để trống", "Cảnh báo");
        configUsername.focus();
        return;
    }

    if (!configDriver.val() || configDriver.val() === "") {
        toastr[Status.Waring]("Driver không được để trống", "Cảnh báo");
        configDriver.focus();
        return;
    }

    if (!configPort.val() || configPort.val() === "") {
        toastr[Status.Waring]("Port không được để trống", "Cảnh báo");
        configPort.focus();
        return;
    }

    if (!configService.val() || configService.val() === "") {
        toastr[Status.Waring]("Service không được để trống", "Cảnh báo");
        configService.focus();
        return;
    }

    // Create object
    const configObject = {
        id: type === 1 ? null : configId.val(),
        name: configName.val(),
        description: configDescription.val(),
        url: configUrl.val(),
        userName: configUsername.val(),
        password: configPassword.val(),
        driver: configDriver.val(),
        port: configPort.val(),
        service: configService.val(),
        createdBy: null,
        createdAt: null,
        updatedBy: null,
        updatedAt: null,
        isDeleted: false,
        isConnected: false,
        isActivated
    }

    // Call API
    insertOrUpdateConfig(configObject, type).then(response => {
        // Close modal
        if (type === 1) {
            $("#addConfigModal .close").click()
        } else if (type === 2) {
            $("#editConfigModal .close").click()
        }

        // Reload Config
        if (sortType !== "" && sortColumn !== "") {
            setTimeout(() => {
                reloadConfig(2)
            }, 300);
        } else {
            setTimeout(() => {
                reloadConfig(1)
            }, 300);
        }
    })
}

// Handle change config status
function changeConfig() {
    // Get Id
    let sourceId = lstId.join(',');
    const sourceStatus = $("#multipleConfigActivate").is(":checked");
    // Call API
    changeConfigStatus(sourceId, sourceStatus).then(response => {
        // Close modal
        $("#changeStatusModal .close").click()
        $("#multipleConfigActivate").removeClass('d-inline-block').addClass('d-none');

        // Reload Config
        if (sortType !== "" && sortColumn !== "") {
            setTimeout(() => {
                reloadConfig(2)
            }, 300);
        } else {
            setTimeout(() => {
                reloadConfig(1)
            }, 300);
        }
    })
}

// Helper function to reload the config page
// reloadType = 1 - getAllConfig
// reloadType = 2 - getConfigOrderBy
function reloadConfig(reloadType) {
    // Add loader
    document.getElementById('preloader-table').style.visibility = 'visible';
    document.getElementById('preloader-table').style.opacity = '1';
    // Call API
    if (reloadType === 1) {
        getAllConfig(currentPage, pageSize).then(response => {
            init(parseJSON(response));
        }).finally(() => {
            setTimeout(() => {
                document.getElementById('preloader-table').style.opacity = '0';
                document.getElementById('preloader-table').style.visibility = 'hidden';
            }, 350)
        })
    } else {
        getConfigOrderBy(sortColumn, sortType, currentPage, pageSize).then(response => {
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
    $("#configName").val("");
    $("#configDescription").val("");
    $("#configUrl").val("");
    $("#configUsername").val("");
    $("#configPassword").val("");
    $("#configDriver").val("");
    $("#configPort").val("");
    $("#configService").val("");
}

// Helper function close change config modal
function closeChangeConfigModal() {
    const changeStatusCheckbox = $("#multipleConfigActivate");
    const isChecked = changeStatusCheckbox.prop("checked");
    changeStatusCheckbox.prop("checked", !isChecked);
}

// Helper function to parse JSON data
function parseJSON(response) {
    if (response && response.length > 0) {
        response = response.map(item => JSON.parse(item));
    }

    return response;
}

// Helper function to fetch list of config
async function getAllConfig(page, pageSize) {
    const response = await interact(`/api/database-config?page=${page}&pageSize=${pageSize}`);
    if (response) {
        return response;
    }
}

// Helper function to fetch a config
async function getSingleConfig(id) {
    const response = await interact(`/api/database-config/${id}`);
    if (response) {
        return response;
    }
}

// Helper function to fetch list of config by order
async function getConfigOrderBy(sortColumn, sortType, page, pageSize) {
    const response = await interact(`/api/database-config?sortColumn=${sortColumn}&sortType=${sortType}&page=${page}&pageSize=${pageSize}`);
    if (response) {
        return response;
    }
}

// Helper function to search config
async function searchConfigAPI(url) {
    const response = await interact(url);
    if (response) {
        return response;
    }
}

// Helper function to fetch list of config
async function insertOrUpdateConfig(configObject, type) {
    const url = type === 1 ? 'create' : `${configObject.id}`;
    const method = type === 1 ? 'POST' : 'PUT';
    const body = JSON.stringify(configObject);
    const response = await interact(`/api/database-config/${url}`, {
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

// Helper function to change config status
async function changeConfigStatus(id, status) {
    const response = await interact(`/api/database-config/updateMany?configId=${id}&status=${status}`, {
        method: 'PUT'
    })
    if (response) {
        return response;
    }
}

// Helper function to check connection status
async function checkConnectionAPI(id) {
    $("#sync-loading").addClass("fa-spin");
    const response = await interact(`/api/database-config/connection/${id}`);
    if (response) {
        $("#sync-loading").removeClass("fa-spin");
        return response;
    }
}