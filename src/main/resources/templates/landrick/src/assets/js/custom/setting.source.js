let sortColumn = "";
let sortType = "";
let lstId = [];
let currentPage = 1;
let pageSize = 10;
const Header = {
    "checkbox": "",
    "name": "Name",
    "description": "Description",
    "content": "Content",
    "type": "Type",
    "isActivated": "Status - Activated",
    "action": "Action"
};
const type = $("#type-container").attr("data-type");

$(document).ready(function () {
    // Get all list of category
    getAllSource(currentPage, pageSize).then(response => {
        init(parseJSON(response));
    });

    $("#changeStatusModal").on('hide.bs.modal', closeChangeSourceModal)
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
    for (let i = 0; i < response["lstSource"].length; i++) {
        const tableBodyRow = $("<tr></tr>");
        for (let j = 0; j < 7; j++) {
            if (j === 0) {
                // Create checkbox heading
                const td = $("<td></td>").addClass("p-3").css("width", "50px");
                const formCheck = $("<div></div>").addClass("form-check");
                const checkBox = $("<input>").attr({
                    type: "checkbox",
                    "data-id": response['lstSource'][i]['id'],
                    "data-status": response['lstSource'][i]['isActivated']
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
                    div.addClass(`badge ${response["lstSource"][i][keys[j]] ? 'bg-soft-success' : 'bg-soft-danger'} rounded px-3 py-1`)
                        .text(`${response["lstSource"][i][keys[j]] ? 'Active' : 'Inactive'}`);

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
                            "data-bs-target": "#editSourceModal",
                            onclick: `${response["lstSource"][i]["editButton"]["callback"]}(${response["lstSource"][i]["id"]})`
                        })
                        .html(response["lstSource"][i]["editButton"]["icon"])
                    // Add button to table
                    div.append(editButton);
                    td.append(div);
                } else {
                    td.text(response["lstSource"][i][keys[j]]);
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
    const multipleSourceActivate = $("#multipleSourceActivate");
    // Handle select all checkbox event
    selectAllCheckBox.click(function () {
        const checked = this.checked;
        $(".form-check > input[type='checkbox']").not("#sourceIsActivated").not(multipleSourceActivate).each(function () {
            this.checked = checked;
            const id = $(this).attr("data-id");
            const listOfChecked = $(".form-check > input[type='checkbox']:checked").not(selectAllCheckBox).not(multipleSourceActivate).not("#sourceIsActivated");
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
                    multipleSourceActivate.removeClass('d-none').addClass("d-inline-block");
                    multipleSourceActivate.prop("checked", true);
                } else if (isAllNotActivated) {
                    multipleSourceActivate.removeClass('d-none').addClass("d-inline-block");
                    multipleSourceActivate.prop("checked", false);
                } else {
                    multipleSourceActivate.removeClass('d-inline-block').addClass('d-none');
                }
            } else {
                multipleSourceActivate.removeClass('d-inline-block').addClass('d-none');
            }
        })
    });

    // Handle checkbox behavior
    $(".form-check > input[type='checkbox']").not("#select-all-checkbox").not("#sourceIsActivated").not(multipleSourceActivate).click(function () {
        const currentId = $(this).attr("data-id");
        const currentChecked = this.checked;
        const listOfCheckbox = $(".form-check > input[type='checkbox']").not(selectAllCheckBox).not("#sourceIsActivated").not(multipleSourceActivate);
        const listOfChecked = $(".form-check > input[type='checkbox']:checked").not(selectAllCheckBox).not("#sourceIsActivated").not(multipleSourceActivate);
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
                multipleSourceActivate.removeClass('d-none').addClass("d-inline-block");
                multipleSourceActivate.prop("checked", true);
            } else if (isAllNotActivated) {
                multipleSourceActivate.removeClass('d-none').addClass("d-inline-block");
                multipleSourceActivate.prop("checked", false);
            } else {
                multipleSourceActivate.removeClass('d-inline-block').addClass('d-none');
            }
        } else {
            multipleSourceActivate.removeClass('d-inline-block').addClass('d-none');
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
                reloadSource(2)
            } else {
                sortType = "desc";
                $(this).attr("data-sort", sortType);
                span.addClass("mdi mdi-sort-descending d-inline-block ms-1");
                $(this).append(span);
                // Reload data
                reloadSource(2)
            }
        }, 150);

    }).on('dblclick', function () {
        clearTimeout(timeout);
        // Clear data
        $(this).attr("data-sort", "");
        sortColumn = "";
        sortType = "";
        // Get all list of category
        reloadSource(1)
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
        reloadSource(2);
    } else {
        reloadSource(1);
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
    reloadSource(1)
}

// Handle search
function searchSource() {
    let searchUrl = `/api/source/search?`
    // Get search value
    const sourceName = $("#searchSourceName").val();
    const sourceDescription = $("#searchSourceDescription").val();
    const sourceContent = $("#searchSourceContent").val();

    // Append search param
    if (sourceName !== "") {
        searchUrl += `name=${sourceName}&`
    }

    if (sourceDescription !== "") {
        searchUrl += `description=${sourceDescription}&`
    }

    if (sourceContent !== "") {
        searchUrl += `content=${sourceContent}&`
    }

    // Remove the trailing '&' if there is one
    if (searchUrl.endsWith('&')) {
        searchUrl = searchUrl.slice(0, -1);
    }

    // Call API
    searchSourceAPI(searchUrl).then(response => {
        init(parseJSON(response));
    })
}

// Helper function to handle open edit modal
function openEditModal(id) {
    getSingleSource(id).then(response => {
        // Get element
        const sourceId = $("#sourceId");
        const sourceName = $("#newSourceName");
        const sourceToken = $("#newSourceToken");
        const sourceDescription = $("#newSourceDescription");
        const sourceContent = $("#newSourceContent");
        const sourceStatus = $("#sourceIsActivated");

        // Set data
        sourceId.val(response["id"]);
        sourceName.val(response["name"]);
        sourceToken.val(response["token"]);
        sourceContent.val(response["content"]);
        sourceDescription.val(response["description"]);
        sourceStatus.prop("checked", response["isActivated"]);
    })
}

// Handle save new or update category
function saveOrUpdateSource(type) {
    // Get Element
    const sourceId = $("#sourceId");
    const sourceName = type === 1 ? $("#sourceName") : $("#newSourceName");
    const sourceDescription = type === 1 ? $("#sourceDescription") : $("#newSourceDescription");
    const sourceToken = type === 1 ? $("#sourceToken") : $("#newSourceToken");
    const sourceContent = type === 1 ? $("#sourceContent") : $("#newSourceContent");
    const isActivated = type === 1 ? true : $("#sourceIsActivated").is(":checked");

    // Validate field
    if (!sourceName.val() || sourceName.val() === "") {
        toastr[Status.Waring]("Tên luồng không được để trống", "Cảnh báo");
        sourceName.focus();
        return;
    }

    if (!sourceContent.val() || sourceContent.val() === "") {
        toastr[Status.Waring]("Nội dung không được để trống", "Cảnh báo");
        sourceContent.focus();
        return;
    }

    // Create object
    const sourceObject = {
        id: type === 1 ? null : sourceId.val(),
        name: sourceName.val(),
        description: sourceDescription.val(),
        content: sourceContent.val(),
        createdBy: null,
        createdAt: null,
        updatedBy: null,
        updatedAt: null,
        isDeleted: null,
        token: sourceToken.val(),
        type: 'API_BASE',
        isActivated
    }

    // Call API
    insertOrUpdateSource(sourceObject, type).then(response => {
        // Close modal
        if (type === 1) {
            $("#addSourceModal .close").click()
        } else if (type === 2) {
            $("#editSourceModal .close").click()
        }

        // Reload Category
        if (sortType !== "" && sortColumn !== "") {
            setTimeout(() => {
                reloadSource(2)
            }, 300);
        } else {
            setTimeout(() => {
                reloadSource(1)
            }, 300);
        }
    })
}

// Handle change flow status
function changeSource() {
    // Get Id
    let sourceId = lstId.join(',');
    const sourceStatus = $("#multipleSourceActivate").is(":checked");
    // Call API
    changeSourceStatus(sourceId, sourceStatus).then(response => {
        // Close modal
        $("#changeStatusModal .close").click()
        $("#multipleSourceActivate").removeClass('d-inline-block').addClass('d-none');

        // Reload Category
        if (sortType !== "" && sortColumn !== "") {
            setTimeout(() => {
                reloadSource(2)
            }, 300);
        } else {
            setTimeout(() => {
                reloadSource(1)
            }, 300);
        }
    })
}

// Helper function to reload the source page
// reloadType = 1 - getAllSource
// reloadType = 2 - getSourceOrderBy
function reloadSource(reloadType) {
    // Add loader
    document.getElementById('preloader-table').style.visibility = 'visible';
    document.getElementById('preloader-table').style.opacity = '1';
    // Call API
    if (reloadType === 1) {
        getAllSource(currentPage, pageSize).then(response => {
            init(parseJSON(response));
        }).finally(() => {
            setTimeout(() => {
                document.getElementById('preloader-table').style.opacity = '0';
                document.getElementById('preloader-table').style.visibility = 'hidden';
            }, 350)
        })
    } else {
        getSourceOrderBy(sortColumn, sortType, currentPage, pageSize).then(response => {
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
    $("#sourceName").val("");
    $("#sourceContent").val("-1");
    $("#sourceDescription").val("");
}

// Helper function close change source modal
function closeChangeSourceModal() {
    const changeStatusCheckbox = $("#multipleSourceActivate");
    const isChecked = changeStatusCheckbox.prop("checked");
    changeStatusCheckbox.prop("checked", !isChecked);
}

// Helper function to parse JSON data
function parseJSON(response) {
    if (response["lstSource"] && response["lstSource"].length > 0) {
        response["lstSource"] = response["lstSource"].map(item => JSON.parse(item));
    }

    return response;
}

// Helper function to fetch list of source
async function getAllSource(page, pageSize) {
    const response = await interact(`/api/source?page=${page}&pageSize=${pageSize}`);
    if (response) {
        return response;
    }
}

// Helper function to fetch a source
async function getSingleSource(id) {
    const response = await interact(`/api/source/${id}`);
    if (response) {
        return response;
    }
}

// Helper function to fetch list of source by order
async function getSourceOrderBy(sortColumn, sortType, page, pageSize) {
    const response = await interact(`/api/source?sortColumn=${sortColumn}&sortType=${sortType}&page=${page}&pageSize=${pageSize}`);
    if (response) {
        return response;
    }
}

// Helper function to search source
async function searchSourceAPI(url) {
    const response = await interact(url);
    if (response) {
        return response;
    }
}

// Helper function to fetch list of source
async function insertOrUpdateSource(sourceObject, type) {
    const url = type === 1 ? 'create' : `${sourceObject.id}`;
    const method = type === 1 ? 'POST' : 'PUT';
    const body = JSON.stringify(sourceObject);
    const response = await interact(`/api/source/${url}`, {
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

// Helper function to change source status
async function changeSourceStatus(id, status) {
    const response = await interact(`/api/source/updateMany?sourceId=${id}&status=${status}`, {
        method: 'PUT'
    })
    if (response) {
        return response;
    }
}