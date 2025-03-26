let sortColumn = "";
let sortType = "";
let lstId = [];
let currentPage = 1;
let pageSize = 10;
const Header = {
    "checkbox": "",
    "name": "Name",
    "type": "Type",
    "content": "Content",
    "isDeleted": "Status - Deleted",
    "isActivated": "Status - Activated",
    "action": "Action"
};
const type = $("#type-container").attr("data-type");

$(document).ready(function () {
    // Get all list of category
    getAllCategory(currentPage, pageSize, type).then(response => {
        init(parseJSON(response));
    });

    // Search box init
    searchBoxInit();
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
    for (let i = 0; i < response["lstCategory"].length; i++) {
        const tableBodyRow = $("<tr></tr>");
        for (let j = 0; j < 7; j++) {
            if (j === 0) {
                // Create checkbox heading
                const td = $("<td></td>").addClass("p-3").css("width", "50px");
                const formCheck = $("<div></div>").addClass("form-check");
                const checkBox = $("<input>").attr({
                    type: "checkbox",
                    "data-id": response['lstCategory'][i]['id']
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
                if (keys[j] === "isActivated" || keys[j] === "isDeleted") {
                    const div = $("<div></div>");
                    // Set title based on key
                    if (keys[j] === "isActivated") {
                        div.addClass(`badge ${response["lstCategory"][i][keys[j]] ? 'bg-soft-success' : 'bg-soft-danger'} rounded px-3 py-1`)
                            .text(`${response["lstCategory"][i][keys[j]] ? 'Active' : 'Inactive'}`);
                    } else {
                        div.addClass(`badge ${response["lstCategory"][i][keys[j]] ? 'bg-soft-danger' : 'bg-soft-success'} rounded px-3 py-1`)
                            .text(`${response["lstCategory"][i][keys[j]] ? 'Deleted' : 'Available'}`);
                    }

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
                            "data-bs-target": "#editCategoryModal",
                            onclick: `${response["lstCategory"][i]["editButton"]["callback"]}(${response["lstCategory"][i]["id"]})`
                        })
                        .html(response["lstCategory"][i]["editButton"]["icon"])
                    const deleteButton = $("<button></button>")
                        .addClass("border-0 bg-transparent")
                        .attr({
                            type: "button",
                            id: "deleteBtn",
                            "data-bs-toggle": "modal",
                            "data-bs-target": "#deleteCategoryModal",
                            onclick: `${response["lstCategory"][i]["deleteButton"]["callback"]}(${response["lstCategory"][i]["id"]})`
                        })
                        .html(response["lstCategory"][i]["deleteButton"]["icon"])
                    // Add button to table
                    div.append(editButton);
                    div.append(deleteButton);
                    td.append(div);
                } else {
                    td.text(response["lstCategory"][i][keys[j]]);
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
    const deleteSelectedBtn = $("#deleteSelectedBtn");
    // Handle select all checkbox event
    selectAllCheckBox.click(function () {
        const checked = this.checked;
        $(".form-check > input[type='checkbox']").each(function () {
            this.checked = checked;
            const id = $(this).attr("data-id");
            const listOfChecked = $(".form-check > input[type='checkbox']:checked").not(selectAllCheckBox);
            if (checked) {
                if (!lstId.includes(id)) {
                    lstId = [...lstId, id];
                }
            } else {
                lstId = lstId.filter(id => id !== $(this).attr("data-id"));
            }
            // Hiện nút xóa all
            if (listOfChecked.length > 0) {
                deleteSelectedBtn.removeClass('d-none').addClass("d-inline-block");
            } else {
                deleteSelectedBtn.removeClass('d-inline-block').addClass('d-none');
            }
        })
    });

    // Handle checkbox behavior
    $(".form-check > input[type='checkbox']:not(#select-all-checkbox)").click(function () {
        const currentId = $(this).attr("data-id");
        const currentChecked = this.checked
        const listOfCheckbox = $(".form-check > input[type='checkbox']").not(selectAllCheckBox);
        const listOfChecked = $(".form-check > input[type='checkbox']:checked").not(selectAllCheckBox);
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
            deleteSelectedBtn.removeClass('d-none').addClass("d-inline-block");
        } else {
            deleteSelectedBtn.removeClass('d-inline-block').addClass('d-none');
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
                reloadCategory(2)
            } else {
                sortType = "desc";
                $(this).attr("data-sort", sortType);
                span.addClass("mdi mdi-sort-descending d-inline-block ms-1");
                $(this).append(span);
                // Reload data
                reloadCategory(2)
            }
        }, 150);

    }).on('dblclick', function () {
        clearTimeout(timeout);
        // Clear data
        $(this).attr("data-sort", "");
        sortColumn = "";
        sortType = "";
        // Get all list of category
        reloadCategory(1)
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

// Helper function to handle search box
function searchBoxInit() {
    const searchCategoryType = $("#searchCategoryType");
    const typeList = type.split(',');
    for (const type of typeList) {
        const option = $("<option></option>").val(type).text(type);
        searchCategoryType.append(option);
    }
}

// Helper function to call API on pagination
function paginationAPI(response, page) {
    currentPage = page;
    // Call API
    if (sortType !== "" && sortColumn !== "") {
        reloadCategory(2);
    } else {
        reloadCategory(1);
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
    reloadCategory(1)
}

// Handle search
function searchCategory() {
    let searchUrl = `/api/categories/search?`
    // Get search value
    const searchCategoryName = $("#searchCategoryName").val();
    const searchCategoryType = $("#searchCategoryType").val();
    const searchCategoryContent = $("#searchCategoryContent").val();
    const searchCategoryDescription = $("#searchCategoryDescription").val();
    const createDateFrom = $("#createDateFrom").val();
    const createDateTo = $("#createDateTo").val();

    // Append search param
    if (searchCategoryName !== "") {
        searchUrl += `name=${searchCategoryName}&`
    }

    if (searchCategoryType !== null && searchCategoryType !== "-1") {
        searchUrl += `type=${searchCategoryType}&`
    } else {
        searchUrl += `type=${type}&`
    }

    if (searchCategoryContent !== "") {
        searchUrl += `content=${searchCategoryContent}&`
    }
    
    if (searchCategoryDescription !== "") {
        searchUrl += `description=${searchCategoryDescription}&`
    }

    if (createDateFrom !== "") {
        searchUrl += `createDateFrom=${createDateFrom}&`
    }

    if (createDateTo !== "") {
        searchUrl += `createDateTo=${createDateTo}&`
    }

    // Remove the trailing '&' if there is one
    if (searchUrl.endsWith('&')) {
        searchUrl = searchUrl.slice(0, -1);
    }

    // Call API
    searchCategoryAPI(searchUrl).then(response => {
        init(parseJSON(response));
    })
}

// Helper function to handle open edit modal
function openEditModal(id) {
    getSingleCategory(id).then(response => {
        // Get element
        const categoryId = $("#categoryId");
        const newCategoryName = $("#newCategoryName");
        const newCategoryType = $("#newCategoryType");
        const newCategoryContent = $("#newCategoryContent");
        const newCategoryDescription = $("#newCategoryDescription");

        // Set data
        categoryId.val(response["id"]);
        newCategoryName.val(response["name"]);
        newCategoryType.val(response["type"]);
        newCategoryContent.val(response["content"]);
        newCategoryDescription.val(response["description"]);
    })
}

// Helper function to handle open delete modal
function openDeleteModal(id) {
    // Get element
    const deleteCategoryId = $("#deleteCategoryId");

    // Set data
    deleteCategoryId.val(id);
}

// Handle save new or update category
function saveOrUpdateCategory(type) {
    // Get Element
    const categoryId = $("#categoryId");
    const categoryName = type === 1 ? $("#categoryName") : $("#newCategoryName");
    const categoryType = type === 1 ? $("#categoryType") : $("#newCategoryType");
    const categoryContent = type === 1 ? $("#categoryContent") : $("#newCategoryContent");
    const categoryDescription = type === 1 ? $("#categoryDescription") : $("#newCategoryDescription");

    // Validate field
    if (!categoryName.val() || categoryName.val() === "") {
        toastr[Status.Waring]("Tên danh mục không được để trống", "Cảnh báo");
        categoryName.focus();
        return;
    }

    if (categoryType.val() === "-1") {
        toastr[Status.Waring]("Loại danh mục không được để trống", "Cảnh báo");
        categoryType.focus();
        return;
    }

    // Create object
    const categoryObject = {
        id: type === 1 ? null : categoryId.val(),
        name: categoryName.val(),
        type: categoryType.val(),
        content: categoryContent.val(),
        description: categoryDescription.val(),
        createdBy: null,
        createdAt: null,
        updatedBy: null,
        updatedAt: null,
        isDeleted: false,
        isActivated: true,
        databaseConfigId: null,
        deleteButton: null,
        editButton: null
    }

    // Call API
    insertOrUpdateCategory(categoryObject, type).then(response => {
        // Close modal
        if (type === 1) {
            $("#addCategoryModal .close").click()
        } else if (type === 2) {
            $("#editCategoryModal .close").click()
        }

        // Reload Category
        if (sortType !== "" && sortColumn !== "") {
            setTimeout(() => {
                reloadCategory(2)
            }, 300);
        } else {
            setTimeout(() => {
                reloadCategory(1)
            }, 300);
        }
    })
}

// Handle save new or update category
function deleteCategory() {
    // Get Id
    let deleteCategoryId;
    if (lstId.length === 0) {
        deleteCategoryId = $("#deleteCategoryId").val();
        // Call API
        deleteSingleCategory(deleteCategoryId).then(response => {
            // Close modal
            $("#deleteCategoryModal .close").click()

            // Reload Category
            if (sortType !== "" && sortColumn !== "") {
                setTimeout(() => {
                    reloadCategory(2)
                }, 300);
            } else {
                setTimeout(() => {
                    reloadCategory(1)
                }, 300);
            }
        })
    } else {
        deleteCategoryId = lstId.join(',');
        // Call API
        deleteCategories(deleteCategoryId).then(response => {
            // Close modal
            $("#deleteCategoryModal .close").click()
            $("#deleteSelectedBtn").removeClass('d-inline-block').addClass('d-none');

            // Reload Category
            if (sortType !== "" && sortColumn !== "") {
                setTimeout(() => {
                    reloadCategory(2)
                }, 300);
            } else {
                setTimeout(() => {
                    reloadCategory(1)
                }, 300);
            }
        })
    }
}

// Helper function to reload the category page
// reloadType = 1 - getAllCategory
// reloadType = 2 - getCategoryOrderBy
function reloadCategory(reloadType) {
    // Add loader
    document.getElementById('preloader-table').style.visibility = 'visible';
    document.getElementById('preloader-table').style.opacity = '1';
    // Call API
    if (reloadType === 1) {
        getAllCategory(currentPage, pageSize, type).then(response => {
            init(parseJSON(response));
        }).finally(() => {
            setTimeout(() => {
                document.getElementById('preloader-table').style.opacity = '0';
                document.getElementById('preloader-table').style.visibility = 'hidden';
            }, 350)
        })
    } else {
        getCategoryOrderBy(sortColumn, sortType, currentPage, pageSize, type).then(response => {
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
    $("#categoryName").val("");
    $("#categoryType").val("-1");
    $("#categoryContent").val("");
    $("#categoryDescription").val("");
}

// Helper function to parse JSON data
function parseJSON(response) {
    if (response["lstCategory"] && response["lstCategory"].length > 0) {
        response["lstCategory"] = response["lstCategory"].map(item => JSON.parse(item));
    }
    return response;
}

// Helper function to fetch list of category
async function getAllCategory(page, pageSize, type) {
    const response = await interact(`/api/categories?page=${page}&pageSize=${pageSize}&type=${type}`);
    if (response) {
        return response;
    }
}

// Helper function to fetch a category
async function getSingleCategory(id) {
    const response = await interact(`/api/categories/${id}`);
    if (response) {
        return response;
    }
}

// Helper function to fetch list of category by order
async function getCategoryOrderBy(sortColumn, sortType, page, pageSize, type) {
    const response = await interact(`/api/categories?sortColumn=${sortColumn}&sortType=${sortType}&page=${page}&pageSize=${pageSize}&type=${type}`);
    if (response) {
        return response;
    }
}

// Helper function to search category
async function searchCategoryAPI(url) {
    const response = await interact(url);
    if (response) {
        return response;
    }
}

// Helper function to delete category
async function deleteSingleCategory(id) {
    const response = await interact(`/api/categories/${id}`, {
        method: 'DELETE'
    })
    if (response) {
        return response;
    }
}

// Helper function to delete multiple categories
async function deleteCategories(ids) {
    const response = await interact(`/api/categories/delete?ids=${ids}`, {
        method: 'DELETE'
    })
    if (response) {
        return response;
    }
}

// Helper function to fetch list of category
async function insertOrUpdateCategory(categoryObject, type) {
    const url = type === 1 ? 'create' : `${categoryObject.id}`;
    const method = type === 1 ? 'POST' : 'PUT';
    const body = JSON.stringify(categoryObject);
    const response = await interact(`/api/categories/${url}`, {
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