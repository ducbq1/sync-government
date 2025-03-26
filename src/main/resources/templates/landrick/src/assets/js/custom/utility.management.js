const sqlQuery = document.getElementById("sqlQuery").value;
const container = document.querySelector('#example');

$(document).ready(function () {
	findAllConfig().then(response => {
		selectionConfigInit(response);
	})
});

async function clearData() {
	$("#sqlQuery").val("");
	await clearTable();
}

async function clearTable() {
	$("#example").empty()
	$("#example").append(`
	<div id="preloader-table" class="position-absolute" style="visibility: hidden; opacity: 0">
		<div id="status-table">
			<div class="spinner">
				<div class="double-bounce1"></div>
				<div class="double-bounce2"></div>
			</div>
		</div>
	</div>
	`)
}

async function doExecuteQuery() {
	await clearTable();
	const databaseConfig = $("#databaseConfig");
	const sqlQuery = $("#sqlQuery");

	if (databaseConfig.val() === "-1") {
		toastr[Status.Waring]("Database không được để trống", "Cảnh báo");
		databaseConfig.focus();
		return;
	}

	if (!sqlQuery.val() || sqlQuery.val() === "") {
		toastr[Status.Waring]("Câu lệnh query không được để trống", "Cảnh báo");
		sqlQuery.focus();
		return;
	}

	const queryDTO = {
		databaseConfigId: databaseConfig.val(),
		query: sqlQuery.val()
	}

	// Add loader
	document.getElementById('preloader-table').style.visibility = 'visible';
	document.getElementById('preloader-table').style.opacity = '1';

	executeQuery(queryDTO).then(async response => {
		if (sqlQuery.val().toLowerCase().startsWith("select")) {
			await createDataTable(response);
		}
	}).finally(() => {
		document.getElementById('preloader-table').style.opacity = '0';
		document.getElementById('preloader-table').style.visibility = 'hidden';
	});
}

async function createDataTable(response) {
	for (const value of response) {
		new Handsontable(container, {
			data: Object.values(value),
			rowHeaders: true,
			colHeaders: Object.keys(value[0]),
			colWidths: 100,
			height: 'auto',
			width: '100%',
			rowHeights: 23,
			stretchH: 'all',
			autoWrapRow: true,
			autoWrapCol: true,
			renderAllRows: false,
			columnSorting: true,
			licenseKey: 'non-commercial-and-evaluation' // for non-commercial use only
		});
	}
}

// Helper function to handle init select config
function selectionConfigInit(response) {
	const databaseConfig = $("#databaseConfig");
	// Add Option
	databaseConfig.empty();
	databaseConfig.append('<option value="-1" selected>-- SELECT A DATABASE --</option>');
	for (const config of response) {
		const databaseOption = $("<option></option>").val(config.id).text(config.name);
		databaseConfig.append(databaseOption);
	}
}

// Helper function to execute query
async function executeQuery(queryDTO) {
	const response = await interact(`/api/utility/sql`, {
		method: 'POST',
		body: JSON.stringify(queryDTO),
		headers: {
			"Content-Type": "application/json",
		},
	});
	if (response) {
		return response;
	}
}

// Helper function to fetch list of database config
async function findAllConfig() {
	const response = await interact(`/api/utility`);
	if (response) {
		return response;
	}
}