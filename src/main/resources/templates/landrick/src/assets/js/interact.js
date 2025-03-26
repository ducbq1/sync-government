const interceptors = {
    request: (url, options) => {
        return [url, options];
    },
    response: async (response) => {
        if (!response.ok) {
            const errorDetails = await response.json();
            toastr[Status.Error]("Cảnh báo", "Có lỗi xảy ra")
            throw new Error(`Error: ${response.status} - ${response.statusText} - ${errorDetails.message}`);
        }
        return response;
    },
    error: (error) => {
        toastr[Status.Error]("Cảnh báo", "Có lỗi xảy ra")
        console.error('Error in customFetch:', error);
    }
};

async function interact(url, options) {
    try {
        const [modifiedUrl, modifiedOptions] = interceptors.request(url, options);
        const response = await fetch(modifiedUrl, modifiedOptions);
        const jsonResponse = await interceptors.response(response).then(res => res.json())
        const {status, title, message, data} = jsonResponse
        if (status && status !== Status.NoMessage) {
            toastr[status](message, title)
        }
        return data
    } catch (error) {
        interceptors.error(error);
    }
};

const Status = {
    Success: "success",
    Info: "info",
    Waring: "warning",
    Error: "error",
    NoMessage: "nomessage"
}

$(document).ready(function () {
    const alertInfo = $('#alert-information');
    if (alertInfo) {
        let info = alertInfo.attr('data-id')
        if (info) {
            toastr.options = {
                "closeButton": true,
                "debug": false,
                "newestOnTop": false,
                "progressBar": true,
                "positionClass": "toast-top-right",
                "preventDuplicates": false,
                "onclick": null,
                "showDuration": "300",
                "hideDuration": "1000",
                "timeOut": "2000",
                "extendedTimeOut": "1000",
                "showEasing": "swing",
                "hideEasing": "linear",
                "showMethod": "fadeIn",
                "hideMethod": "fadeOut"
            }
            const {status, title, message} = JSON.parse(info.replaceAll('&quote;', '\"'));
            if (status && status !== Status.NoMessage) {
                toastr[status](message, title)
            }

            interact('/api/session/clear-toast');
        }
    }
});

function buildTree(categories) {
    const map = {};
    const roots = [];
    categories.forEach(category => {
        category.children = [];
        map[category.id] = category;
    });
    categories.forEach(category => {
        if (category.parentId == 0) {
            roots.push(category);
        } else {
            const parent = map[category.parentId];
            if (parent) {
                parent.children.push(category);
            }
        }
    });

    roots.sort((a, b) => a.ordinal - b.ordinal);
    return roots;
}

function renderTree(categories, parentElement) {
    categories.forEach(category => {

        const a = document.createElement('a');
        a.setAttribute('href', category.children.length > 0 ? 'javascript:void(0)' : category.url);

        if (category.parentId == 0 && category.icon) {
            const i = document.createElement('i')
            i.classList.add(...category.icon.split(' '));
            a.append(i);
        }

        a.append(document.createTextNode(category.title));

        const li = document.createElement('li');
        li.append(a);

        if (category.children.length > 0) {

            li.classList.add('sidebar-dropdown');
            const ul = document.createElement('ul');
            renderTree(category.children, ul);
            const div = document.createElement('div')
            div.classList.add('sidebar-submenu')
            div.append(ul);
            li.append(div)
        }

        parentElement.appendChild(li);
    });
}

$(document).ready(function () {
    const root = document.querySelector('#menu-tree')
    const menu = JSON.parse($('#menus')?.attr('data-id')?.replaceAll('&quote;', '\"') ?? "[]")
    if (root && menu) {
        const tree = buildTree(menu);
        renderTree(tree, root);

        if (document.getElementById("sidebar")) {
            const elements = document.getElementById("sidebar").getElementsByTagName("a");
            for (let i = 0, len = elements.length; i < len; i++) {
                elements[i].onclick = function (elem) {
                    if (elem.target !== document.querySelectorAll("li.sidebar-dropdown.active > a")[0]) {
                        document.querySelectorAll("li.sidebar-dropdown.active")[0]?.classList?.toggle("active");
                        document.querySelectorAll("div.sidebar-submenu.d-block")[0]?.classList?.toggle("d-block");
                    }
                    if (elem.target.getAttribute("href") === "javascript:void(0)") {
                        elem.target.parentElement.classList.toggle("active");
                        elem.target.nextElementSibling.classList.toggle("d-block");
                    }
                }
            }
        }
    }
});


