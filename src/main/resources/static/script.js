/*
[L2] ============================================================
[L3] VISITOR & EMPLOYEE ACCESS MANAGEMENT SYSTEM
[L4] FINAL FRONTEND SCRIPT
[L5] ============================================================
*/


/* =========================================================
   GLOBAL STATE
========================================================= */

let loggedInEmployee = null;

let currentVisitorRequestId =
    sessionStorage.getItem("visitorRequestId") || null;

let currentVisitorAccessToken =
    sessionStorage.getItem("visitorAccessToken") || null;

let visitorStatusTimer = null;

let employeeRefreshTimer = null;

let employeeStatistics = {
    pending: 0,
    approved: 0,
    rejected: 0
};


/* =========================================================
   DOM ELEMENTS
========================================================= */

const roleSelection =
    document.getElementById("roleSelection");

const visitorPortal =
    document.getElementById("visitorPortal");

const employeeLogin =
    document.getElementById("employeeLogin");

const employeeWorkspace =
    document.getElementById("employeeWorkspace");

const form =
    document.getElementById("visitorForm");

const message =
    document.getElementById("message");

const loginForm =
    document.getElementById("loginForm");

const loginMessage =
    document.getElementById("loginMessage");

const employeeTableBody =
    document.getElementById("employeeTableBody");

const historyTableBody =
    document.getElementById("historyTableBody");

const noPendingRequests =
    document.getElementById("noPendingRequests");

const noHistoryRequests =
    document.getElementById("noHistoryRequests");


/* =========================================================
   UTILITY
========================================================= */

function escapeHtml(value) {

    if (
        value === null ||
        value === undefined
    ) {
        return "";
    }

    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}


function setMessage(
    element,
    text,
    type = "hidden"
) {

    if (!element) {
        return;
    }

    element.innerHTML = text;

    if (type === "hidden") {

        element.className =
            "message hidden";

    } else {

        element.className =
            `message ${type}`;
    }
}


/* =========================================================
   VISITOR TIMER
========================================================= */

function stopVisitorStatusRefresh() {

    if (visitorStatusTimer !== null) {

        clearInterval(
            visitorStatusTimer
        );

        visitorStatusTimer = null;
    }
}


/* =========================================================
   EMPLOYEE TIMER
========================================================= */

function stopEmployeeAutoRefresh() {

    if (employeeRefreshTimer !== null) {

        clearInterval(
            employeeRefreshTimer
        );

        employeeRefreshTimer = null;
    }
}


/* =========================================================
   SCREEN MANAGEMENT
========================================================= */

function hideAllScreens() {

    if (roleSelection) {
        roleSelection.classList.add("hidden");
    }

    if (visitorPortal) {
        visitorPortal.classList.add("hidden");
    }

    if (employeeLogin) {
        employeeLogin.classList.add("hidden");
    }

    if (employeeWorkspace) {
        employeeWorkspace.classList.add("hidden");
    }

    stopVisitorStatusRefresh();
    stopEmployeeAutoRefresh();
}


function showRoleSelection() {

    hideAllScreens();

    if (roleSelection) {
        roleSelection.classList.remove("hidden");
    }
}


/* =========================================================
   VISITOR SESSION
========================================================= */

function restoreVisitorSession() {

    currentVisitorRequestId =
        sessionStorage.getItem(
            "visitorRequestId"
        ) || null;

    currentVisitorAccessToken =
        sessionStorage.getItem(
            "visitorAccessToken"
        ) || null;
}


function saveVisitorSession(
    requestId,
    accessToken
) {

    currentVisitorRequestId =
        requestId;

    currentVisitorAccessToken =
        accessToken;

    sessionStorage.setItem(
        "visitorRequestId",
        String(requestId)
    );

    sessionStorage.setItem(
        "visitorAccessToken",
        accessToken
    );
}


function clearVisitorSession() {

    sessionStorage.removeItem(
        "visitorRequestId"
    );

    sessionStorage.removeItem(
        "visitorAccessToken"
    );

    currentVisitorRequestId = null;

    currentVisitorAccessToken = null;

    stopVisitorStatusRefresh();

    clearVisitorStatusDisplay();
}


/* =========================================================
   VISITOR STATUS DISPLAY
========================================================= */

function clearVisitorStatusDisplay() {

    const statusResult =
        document.getElementById(
            "statusResult"
        );

    const statusId =
        document.getElementById(
            "statusId"
        );

    const statusName =
        document.getElementById(
            "statusName"
        );

    const statusPerson =
        document.getElementById(
            "statusPerson"
        );

    const statusPurpose =
        document.getElementById(
            "statusPurpose"
        );

    const statusValue =
        document.getElementById(
            "statusValue"
        );


    if (statusId) {
        statusId.innerText = "";
    }

    if (statusName) {
        statusName.innerText = "";
    }

    if (statusPerson) {
        statusPerson.innerText = "";
    }

    if (statusPurpose) {
        statusPurpose.innerText = "";
    }

    if (statusValue) {
        statusValue.innerText = "";
        statusValue.style.color = "";
    }

    if (statusResult) {
        statusResult.classList.add("hidden");
    }
}


/* =========================================================
   VISITOR PORTAL
========================================================= */

function showVisitorPortal() {

    hideAllScreens();

    if (visitorPortal) {
        visitorPortal.classList.remove("hidden");
    }

    clearVisitorStatusDisplay();

    stopVisitorStatusRefresh();

    const statusRequestId =
        document.getElementById(
            "statusRequestId"
        );

    const requestIdBox =
        document.getElementById(
            "requestIdBox"
        );

    const generatedRequestId =
        document.getElementById(
            "generatedRequestId"
        );


    if (statusRequestId) {
        statusRequestId.value = "";
    }

    if (requestIdBox) {
        requestIdBox.classList.add("hidden");
    }

    if (generatedRequestId) {
        generatedRequestId.innerText = "-";
    }

    setMessage(
        message,
        "",
        "hidden"
    );


    /*
     * Load employees every time the visitor portal opens.
     * This keeps the dropdown synchronized with the database.
     */

    loadEmployees();
}


/* =========================================================
   LOAD EMPLOYEES
========================================================= */

async function loadEmployees() {

    const employeeSelect =
        document.getElementById(
            "personToMeet"
        );

    if (!employeeSelect) {
        return;
    }


    employeeSelect.innerHTML = `
        <option value="" disabled selected>
            Loading employees...
        </option>
    `;


    try {

        const response =
            await fetch(
                `/employees?time=${Date.now()}`,
                {
                    method: "GET",

                    cache: "no-store",

                    headers: {
                        "Cache-Control":
                            "no-cache",

                        "Pragma":
                            "no-cache"
                    }
                }
            );


        if (!response.ok) {

            throw new Error(
                "Unable to load employees."
            );
        }


        const employees =
            await response.json();


        employeeSelect.innerHTML = `
            <option value="" disabled selected>
                Select employee
            </option>
        `;


        if (
            !Array.isArray(employees) ||
            employees.length === 0
        ) {

            employeeSelect.innerHTML = `
                <option value="" disabled selected>
                    No employees available
                </option>
            `;

            return;
        }


        employees.forEach(
            employee => {

                /*
                 * IMPORTANT:
                 * Employee ID is the option value.
                 * This becomes assignedEmployeeId.
                 */

                if (
                    !employee ||
                    !employee.name ||
                    !employee.employeeId
                ) {
                    return;
                }


                const option =
                    document.createElement(
                        "option"
                    );


                option.value =
                    employee.employeeId;


                option.textContent =
                    `${employee.name} (${employee.employeeId})`;


                employeeSelect.appendChild(
                    option
                );
            }
        );


    } catch (error) {

        console.error(
            "Employee loading error:",
            error
        );


        employeeSelect.innerHTML = `
            <option value="" disabled selected>
                Unable to load employees
            </option>
        `;
    }
}


/* =========================================================
   VISITOR REGISTRATION
========================================================= */

if (form) {

    form.addEventListener(
        "submit",
        async function(event) {

            event.preventDefault();

            stopVisitorStatusRefresh();

            clearVisitorStatusDisplay();


            const employeeSelect =
                document.getElementById(
                    "personToMeet"
                );


            if (!employeeSelect) {

                setMessage(
                    message,
                    "Employee selection field not found.",
                    "error"
                );

                return;
            }


            const selectedEmployee =
                employeeSelect.options[
                    employeeSelect.selectedIndex
                ];


            if (
                !selectedEmployee ||
                !selectedEmployee.value
            ) {

                setMessage(
                    message,
                    "Please select an employee.",
                    "error"
                );

                return;
            }


            /*
             * Employee ID is the option value.
             */

            const assignedEmployeeId =
                selectedEmployee.value.trim();


            /*
             * Employee name is displayed as:
             *
             * Rahul (EMP002)
             *
             * We only send:
             *
             * Rahul
             */

            const personToMeet =
                selectedEmployee.textContent
                    .replace(
                        /\s*\(.*?\)\s*$/,
                        ""
                    )
                    .trim();


            if (
                !assignedEmployeeId ||
                !personToMeet
            ) {

                setMessage(
                    message,
                    "Please select a valid employee.",
                    "error"
                );

                return;
            }


            const visitor = {

                name:
                    document
                        .getElementById("name")
                        .value
                        .trim(),

                email:
                    document
                        .getElementById("email")
                        .value
                        .trim(),

                phone:
                    document
                        .getElementById("phone")
                        .value
                        .trim(),

                purpose:
                    document
                        .getElementById("purpose")
                        .value
                        .trim(),

                personToMeet:
                    personToMeet,

                assignedEmployeeId:
                    assignedEmployeeId
            };


            console.log(
                "Submitting visitor request:",
                visitor
            );


            try {

                const response =
                    await fetch(
                        "/users",
                        {
                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body:
                                JSON.stringify(
                                    visitor
                                ),

                            cache:
                                "no-store"
                        }
                    );


                if (!response.ok) {

                    let errorMessage =
                        "Registration failed.";


                    try {

                        const errorData =
                            await response.json();


                        if (
                            errorData &&
                            errorData.message
                        ) {

                            errorMessage =
                                errorData.message;
                        }

                    } catch (parseError) {

                        console.error(
                            "Error reading server response:",
                            parseError
                        );
                    }


                    throw new Error(
                        errorMessage
                    );
                }


                const savedVisitor =
                    await response.json();


                if (
                    !savedVisitor.id
                ) {

                    throw new Error(
                        "Request ID was not returned by the server."
                    );
                }


                currentVisitorRequestId =
                    savedVisitor.id;


                currentVisitorAccessToken =
                    savedVisitor.accessToken ||
                    null;


                saveVisitorSession(
                    savedVisitor.id,
                    savedVisitor.accessToken ||
                    null
                );


                if (message) {

                    message.innerHTML =
                        "✓ Visit request submitted successfully!";

                    message.className =
                        "message success";
                }


                const requestIdBox =
                    document.getElementById(
                        "requestIdBox"
                    );


                const generatedRequestId =
                    document.getElementById(
                        "generatedRequestId"
                    );


                if (generatedRequestId) {

                    generatedRequestId.innerText =
                        savedVisitor.id;
                }


                if (requestIdBox) {

                    requestIdBox.classList.remove(
                        "hidden"
                    );
                }


                const statusRequestId =
                    document.getElementById(
                        "statusRequestId"
                    );


                if (statusRequestId) {

                    statusRequestId.value =
                        savedVisitor.id;
                }


                form.reset();


                await checkRequestStatus();


                startVisitorStatusRefresh();


            } catch (error) {

                console.error(
                    "Registration error:",
                    error
                );


                setMessage(
                    message,
                    error.message ||
                        "Something went wrong. Please try again.",
                    "error"
                );
            }
        }
    );
}


/* =========================================================
   VISITOR STATUS CHECK
========================================================= */

async function checkRequestStatus() {

    const statusRequestId =
        document.getElementById(
            "statusRequestId"
        );

    const statusMessage =
        document.getElementById(
            "statusMessage"
        );

    const statusResult =
        document.getElementById(
            "statusResult"
        );


    if (!statusRequestId) {
        return;
    }


    const requestId =
        statusRequestId.value.trim();


    if (!requestId) {

        setMessage(
            statusMessage,
            "Please enter your Request ID.",
            "error"
        );

        return;
    }


    try {

        const response =
            await fetch(
                `/users/${encodeURIComponent(
                    requestId
                )}/status?time=${Date.now()}`,
                {
                    method: "GET",

                    cache: "no-store",

                    headers: {

                        "X-Access-Token":
                            currentVisitorAccessToken,

                        "Cache-Control":
                            "no-cache",

                        "Pragma":
                            "no-cache"
                    }
                }
            );


        if (response.status === 404) {

            setMessage(
                statusMessage,
                "Request ID not found.",
                "error"
            );

            if (statusResult) {

                statusResult.classList.add(
                    "hidden"
                );
            }

            return;
        }


        if (!response.ok) {

            throw new Error(
                "Could not check request status."
            );
        }


        const visitor =
            await response.json();


        currentVisitorRequestId =
            visitor.id;


        const statusId =
            document.getElementById(
                "statusId"
            );

        const statusName =
            document.getElementById(
                "statusName"
            );

        const statusPerson =
            document.getElementById(
                "statusPerson"
            );

        const statusPurpose =
            document.getElementById(
                "statusPurpose"
            );

        const statusValue =
            document.getElementById(
                "statusValue"
            );


        if (statusId) {

            statusId.innerText =
                visitor.id;
        }


        if (statusName) {

            statusName.innerText =
                visitor.name || "";
        }


        if (statusPerson) {

            statusPerson.innerText =
                visitor.personToMeet || "";
        }


        if (statusPurpose) {

            statusPurpose.innerText =
                visitor.purpose || "";
        }


        const status =
            String(
                visitor.status ||
                "PENDING"
            ).toUpperCase();


        if (statusValue) {

            statusValue.innerText =
                status;


            if (status === "APPROVED") {

                statusValue.style.color =
                    "green";

            } else if (
                status === "REJECTED"
            ) {

                statusValue.style.color =
                    "red";

            } else {

                statusValue.style.color =
                    "#b7791f";
            }
        }


        setMessage(
            statusMessage,
            "",
            "hidden"
        );


        if (statusResult) {

            statusResult.classList.remove(
                "hidden"
            );
        }


        if (
            status === "APPROVED" ||
            status === "REJECTED"
        ) {

            stopVisitorStatusRefresh();
        }


    } catch (error) {

        console.error(
            "Status check error:",
            error
        );


        if (
            statusMessage &&
            (
                !statusResult ||
                statusResult.classList.contains(
                    "hidden"
                )
            )
        ) {

            setMessage(
                statusMessage,
                "Unable to check request status. Please try again.",
                "error"
            );
        }
    }
}


/* =========================================================
   VISITOR AUTO STATUS REFRESH
========================================================= */

function startVisitorStatusRefresh() {

    stopVisitorStatusRefresh();


    visitorStatusTimer =
        setInterval(
            async function() {

                if (
                    !currentVisitorRequestId ||
                    !visitorPortal ||
                    visitorPortal.classList.contains(
                        "hidden"
                    )
                ) {

                    stopVisitorStatusRefresh();

                    return;
                }


                const statusRequestId =
                    document.getElementById(
                        "statusRequestId"
                    );


                if (statusRequestId) {

                    statusRequestId.value =
                        currentVisitorRequestId;
                }


                await checkRequestStatus();

            },
            3000
        );
}


/* =========================================================
   EMPLOYEE LOGIN SCREEN
========================================================= */

function showEmployeeLogin() {

    hideAllScreens();

    if (employeeLogin) {

        employeeLogin.classList.remove(
            "hidden"
        );
    }


    setMessage(
        loginMessage,
        "",
        "hidden"
    );
}


/* =========================================================
   EMPLOYEE LOGIN
========================================================= */

if (loginForm) {

    loginForm.addEventListener(
        "submit",
        async function(event) {

            event.preventDefault();


            const employeeId =
                document
                    .getElementById(
                        "employeeId"
                    )
                    .value
                    .trim();


            const password =
                document
                    .getElementById(
                        "employeePassword"
                    )
                    .value;


            if (
                !employeeId ||
                !password
            ) {

                setMessage(
                    loginMessage,
                    "Please enter Employee ID and Password.",
                    "error"
                );

                return;
            }


            try {

                const response =
                    await fetch(
                        "/employee/login",
                        {
                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body:
                                JSON.stringify({
                                    employeeId:
                                        employeeId,

                                    password:
                                        password
                                }),

                            cache:
                                "no-store"
                        }
                    );


                if (!response.ok) {

                    let errorMessage =
                        "Invalid Employee ID or Password.";


                    try {

                        const serverMessage =
                            await response.text();


                        if (serverMessage) {

                            errorMessage =
                                serverMessage;
                        }

                    } catch (error) {

                        console.error(
                            error
                        );
                    }


                    setMessage(
                        loginMessage,
                        errorMessage,
                        "error"
                    );

                    return;
                }


                loggedInEmployee =
                    await response.json();


                setMessage(
                    loginMessage,
                    "",
                    "hidden"
                );


                showEmployeeWorkspace();


            } catch (error) {

                console.error(
                    "Login error:",
                    error
                );


                setMessage(
                    loginMessage,
                    "Unable to login. Please try again.",
                    "error"
                );
            }
        }
    );
}


/* =========================================================
   EMPLOYEE WORKSPACE
========================================================= */

function showEmployeeWorkspace() {

    hideAllScreens();


    if (employeeWorkspace) {

        employeeWorkspace.classList.remove(
            "hidden"
        );
    }


    if (loggedInEmployee) {

        const employeeName =
            document.getElementById(
                "employeeName"
            );


        if (employeeName) {

            employeeName.innerText =
                loggedInEmployee.name ||
                "Employee";
        }
    }


    loadEmployeeRequests();

    loadEmployeeHistory();

    startEmployeeAutoRefresh();
}


/* =========================================================
   LOAD ACTIVE EMPLOYEE REQUESTS
========================================================= */

async function loadEmployeeRequests() {

    if (!loggedInEmployee) {
        return;
    }


    try {

        const response =
            await fetch(
                `/users?time=${Date.now()}`,
                {
                    method: "GET",

                    cache: "no-store",

                    headers: {
                        "Cache-Control":
                            "no-cache",

                        "Pragma":
                            "no-cache"
                    }
                }
            );


        if (response.status === 401) {

            loggedInEmployee = null;

            stopEmployeeAutoRefresh();

            showEmployeeLogin();

            return;
        }


        if (!response.ok) {

            throw new Error(
                "Could not load visitor requests."
            );
        }


        const allVisitors =
            await response.json();


        if (!employeeTableBody) {
            return;
        }


        /*
         * =====================================================
         * IMPORTANT EMPLOYEE FILTER
         * =====================================================
         *
         * /users returns visitor requests.
         *
         * Each request contains:
         *
         *     assignedEmployeeId
         *
         * The logged-in employee contains:
         *
         *     employeeId
         *
         * Only requests belonging to this employee
         * are displayed.
         */

        const currentEmployeeId =
            String(
                loggedInEmployee.employeeId ||
                ""
            ).trim();


        const visitors =
            Array.isArray(allVisitors)
                ? allVisitors.filter(
                    visitor => {

                        const assignedEmployeeId =
                            String(
                                visitor.assignedEmployeeId ||
                                ""
                            ).trim();


                        return (
                            currentEmployeeId !== "" &&
                            assignedEmployeeId ===
                                currentEmployeeId
                        );
                    }
                )
                : [];


        employeeTableBody.innerHTML =
            "";


        let pending = 0;

        let approved = 0;

        let rejected = 0;


        /*
         * Only PENDING requests are displayed
         * in Active Visit Requests.
         */

        visitors.forEach(
            visitor => {

                const status =
                    String(
                        visitor.status ||
                        "PENDING"
                    ).toUpperCase();


                if (status === "PENDING") {

                    pending++;


                    const row =
                        document.createElement(
                            "tr"
                        );


                    row.innerHTML = `

                        <td>
                            ${escapeHtml(
                                visitor.id
                            )}
                        </td>

                        <td>
                            ${escapeHtml(
                                visitor.name
                            )}
                        </td>

                        <td>
                            ${escapeHtml(
                                visitor.email
                            )}
                        </td>

                        <td>
                            ${escapeHtml(
                                visitor.phone ||
                                "-"
                            )}
                        </td>

                        <td>
                            ${escapeHtml(
                                visitor.purpose ||
                                "-"
                            )}
                        </td>

                        <td>
                            <span class="status pending">
                                PENDING
                            </span>
                        </td>

                        <td>

                            <button
                                type="button"
                                class="approve-btn"
                                onclick="approveVisitor(${Number(
                                    visitor.id
                                )})">

                                Approve

                            </button>

                            <button
                                type="button"
                                class="reject-btn"
                                onclick="rejectVisitor(${Number(
                                    visitor.id
                                )})">

                                Reject

                            </button>

                        </td>
                    `;


                    employeeTableBody.appendChild(
                        row
                    );
                }


                if (status === "APPROVED") {

                    approved++;
                }


                if (status === "REJECTED") {

                    rejected++;
                }
            }
        );


        /*
         * EMPTY STATE
         */

        if (
            noPendingRequests
        ) {

            if (pending === 0) {

                noPendingRequests.classList.remove(
                    "hidden"
                );

            } else {

                noPendingRequests.classList.add(
                    "hidden"
                );
            }
        }


        /*
         * COUNTERS
         */

        const pendingCount =
            document.getElementById(
                "pendingCount"
            );

        const approvedCount =
            document.getElementById(
                "approvedCount"
            );

        const rejectedCount =
            document.getElementById(
                "rejectedCount"
            );


        if (pendingCount) {

            pendingCount.innerText =
                pending;
        }


        if (approvedCount) {

            approvedCount.innerText =
                approved;
        }


        if (rejectedCount) {

            rejectedCount.innerText =
                rejected;
        }


    } catch (error) {

        console.error(
            "Error loading employee requests:",
            error
        );
    }
}


/* =========================================================
   EMPLOYEE HISTORY
========================================================= */

/*
 * IMPORTANT FIX:
 *
 * The current backend DOES NOT have:
 *
 *     GET /employee/history
 *
 * So the old frontend call to /employee/history
 * was returning 404 and the function was simply
 * returning without rendering anything.
 *
 * We therefore use the existing employee-protected
 * GET /users endpoint as the history source.
 *
 * Then:
 *
 *     assignedEmployeeId
 *              +
 *     current employeeId
 *
 * are used to select this employee's records.
 *
 * Finally:
 *
 *     APPROVED / REJECTED
 *
 * are rendered in Request History.
 */

async function loadEmployeeHistory() {

    if (!loggedInEmployee) {
        return;
    }


    try {

        const response =
            await fetch(
                `/users?time=${Date.now()}`,
                {
                    method: "GET",

                    cache: "no-store",

                    headers: {
                        "Cache-Control":
                            "no-cache",

                        "Pragma":
                            "no-cache"
                    }
                }
            );


        if (
            response.status === 401
        ) {

            loggedInEmployee = null;

            stopEmployeeAutoRefresh();

            showEmployeeLogin();

            return;
        }


        if (!response.ok) {

            throw new Error(
                "Could not load employee history."
            );
        }


        const allUsers =
            await response.json();


        if (!historyTableBody) {
            return;
        }


        /*
         * =====================================================
         * FILTER HISTORY FOR CURRENT EMPLOYEE
         * =====================================================
         *
         * Only requests assigned to the currently
         * logged-in employee are considered.
         */

        const currentEmployeeId =
            String(
                loggedInEmployee.employeeId ||
                ""
            ).trim();


        const history =
            Array.isArray(allUsers)
                ? allUsers.filter(
                    visitor => {

                        const assignedEmployeeId =
                            String(
                                visitor.assignedEmployeeId ||
                                ""
                            ).trim();


                        return (
                            currentEmployeeId !== "" &&
                            assignedEmployeeId ===
                                currentEmployeeId
                        );
                    }
                )
                : [];


        /*
         * Clear old history rows.
         */

        historyTableBody.innerHTML =
            "";


        let historyCount = 0;


        /*
         * Render only APPROVED and REJECTED.
         *
         * PENDING stays in Active Visit Requests.
         */

        history.forEach(
            visitor => {

                const status =
                    String(
                        visitor.status ||
                        ""
                    ).toUpperCase();


                if (
                    status !== "APPROVED" &&
                    status !== "REJECTED"
                ) {

                    return;
                }


                historyCount++;


                const row =
                    document.createElement(
                        "tr"
                    );


                const statusClass =
                    status === "APPROVED"
                        ? "approved"
                        : "rejected";


                row.innerHTML = `

                    <td>
                        ${escapeHtml(
                            visitor.id
                        )}
                    </td>

                    <td>
                        ${escapeHtml(
                            visitor.name
                        )}
                    </td>

                    <td>
                        ${escapeHtml(
                            visitor.email
                        )}
                    </td>

                    <td>
                        ${escapeHtml(
                            visitor.phone ||
                            "-"
                        )}
                    </td>

                    <td>
                        ${escapeHtml(
                            visitor.purpose ||
                            "-"
                        )}
                    </td>

                    <td>

                        <span
                            class="status ${statusClass}">

                            ${escapeHtml(
                                status
                            )}

                        </span>

                    </td>
                `;


                historyTableBody.appendChild(
                    row
                );
            }
        );


        /*
         * EMPTY HISTORY STATE
         */

        if (
            noHistoryRequests
        ) {

            if (historyCount === 0) {

                noHistoryRequests.classList.remove(
                    "hidden"
                );

            } else {

                noHistoryRequests.classList.add(
                    "hidden"
                );
            }
        }


    } catch (error) {

        console.error(
            "Error loading employee history:",
            error
        );
    }
}


/* =========================================================
   EMPLOYEE AUTO REFRESH
========================================================= */

function startEmployeeAutoRefresh() {

    stopEmployeeAutoRefresh();


    employeeRefreshTimer =
        setInterval(
            async function() {

                if (
                    !employeeWorkspace ||
                    employeeWorkspace.classList.contains(
                        "hidden"
                    )
                ) {

                    return;
                }


                if (!loggedInEmployee) {
                    return;
                }


                /*
                 * Refresh both tables.
                 *
                 * Active:
                 *     PENDING
                 *
                 * History:
                 *     APPROVED / REJECTED
                 */

                await Promise.all([
                    loadEmployeeRequests(),
                    loadEmployeeHistory()
                ]);

            },
            5000
        );
}


/* =========================================================
   APPROVE VISITOR
========================================================= */

async function approveVisitor(id) {

    const confirmed =
        confirm(
            "Are you sure you want to approve this visitor?"
        );


    if (!confirmed) {
        return;
    }


    try {

        const response =
            await fetch(
                `/users/${encodeURIComponent(
                    id
                )}/approve`,
                {
                    method: "PUT",

                    cache: "no-store"
                }
            );


        if (
            response.status === 401
        ) {

            loggedInEmployee = null;

            stopEmployeeAutoRefresh();

            showEmployeeLogin();

            return;
        }


        if (!response.ok) {

            throw new Error(
                "Approval failed."
            );
        }


        alert(
            "Visitor approved successfully!"
        );


        /*
         * IMPORTANT:
         *
         * The request is now:
         *
         *     PENDING
         *        ↓
         *     APPROVED
         *
         * So:
         *
         *     Active table
         *     gets refreshed
         *
         *     History table
         *     gets refreshed
         *
         * The approved request will therefore
         * disappear from Active and appear in History.
         */

        await Promise.all([
            loadEmployeeRequests(),
            loadEmployeeHistory()
        ]);


    } catch (error) {

        console.error(
            "Approval error:",
            error
        );


        alert(
            "Could not approve visitor."
        );
    }
}


/* =========================================================
   REJECT VISITOR
========================================================= */

async function rejectVisitor(id) {

    const confirmed =
        confirm(
            "Are you sure you want to reject this visitor?"
        );


    if (!confirmed) {
        return;
    }


    try {

        const response =
            await fetch(
                `/users/${encodeURIComponent(
                    id
                )}/reject`,
                {
                    method: "PUT",

                    cache: "no-store"
                }
            );


        if (
            response.status === 401
        ) {

            loggedInEmployee = null;

            stopEmployeeAutoRefresh();

            showEmployeeLogin();

            return;
        }


        if (!response.ok) {

            throw new Error(
                "Rejection failed."
            );
        }


        alert(
            "Visitor rejected successfully!"
        );


        /*
         * Same flow as approval:
         *
         *     PENDING
         *        ↓
         *     REJECTED
         *
         * Active gets cleared.
         * History gets updated.
         */

        await Promise.all([
            loadEmployeeRequests(),
            loadEmployeeHistory()
        ]);


    } catch (error) {

        console.error(
            "Rejection error:",
            error
        );


        alert(
            "Could not reject visitor."
        );
    }
}


/* =========================================================
   EMPLOYEE LOGOUT
========================================================= */

async function logoutEmployee() {

    try {

        await fetch(
            "/employee/logout",
            {
                method: "POST",

                cache: "no-store"
            }
        );

    } catch (error) {

        console.error(
            "Logout request failed:",
            error
        );
    }


    loggedInEmployee = null;

    stopEmployeeAutoRefresh();


    if (loginForm) {

        loginForm.reset();
    }


    showRoleSelection();
}


/* =========================================================
   CHECK EXISTING EMPLOYEE LOGIN
========================================================= */

async function checkExistingLogin() {

    try {

        const response =
            await fetch(
                `/employee/me?time=${Date.now()}`,
                {
                    method: "GET",

                    cache: "no-store",

                    headers: {
                        "Cache-Control":
                            "no-cache",

                        "Pragma":
                            "no-cache"
                    }
                }
            );


        if (!response.ok) {

            showRoleSelection();

            return;
        }


        loggedInEmployee =
            await response.json();


        showEmployeeWorkspace();


    } catch (error) {

        console.error(
            "Existing login check failed:",
            error
        );


        showRoleSelection();
    }
}


/* =========================================================
   BROWSER PAGE RESTORATION
========================================================= */

window.addEventListener(
    "pageshow",
    function() {

        setTimeout(
            function() {

                /*
                 * VISITOR PORTAL
                 */

                if (
                    visitorPortal &&
                    !visitorPortal.classList.contains(
                        "hidden"
                    )
                ) {

                    if (
                        currentVisitorRequestId
                    ) {

                        const statusRequestId =
                            document.getElementById(
                                "statusRequestId"
                            );


                        if (statusRequestId) {

                            statusRequestId.value =
                                currentVisitorRequestId;
                        }


                        checkRequestStatus();

                        startVisitorStatusRefresh();
                    }


                    /*
                     * Refresh employee list when
                     * visitor returns to the page.
                     */

                    loadEmployees();
                }


                /*
                 * EMPLOYEE WORKSPACE
                 */

                if (
                    employeeWorkspace &&
                    !employeeWorkspace.classList.contains(
                        "hidden"
                    )
                ) {

                    if (loggedInEmployee) {

                        loadEmployeeRequests();

                        loadEmployeeHistory();

                        startEmployeeAutoRefresh();
                    }
                }

            },
            300
        );
    }
);


/* =========================================================
   WHEN USER RETURNS TO TAB
========================================================= */

document.addEventListener(
    "visibilitychange",
    function() {

        if (
            document.visibilityState !==
            "visible"
        ) {

            return;
        }


        /*
         * VISITOR PAGE
         */

        if (
            visitorPortal &&
            !visitorPortal.classList.contains(
                "hidden"
            )
        ) {

            /*
             * Keep employee list current.
             */

            loadEmployees();


            if (
                currentVisitorRequestId
            ) {

                const statusRequestId =
                    document.getElementById(
                        "statusRequestId"
                    );


                if (statusRequestId) {

                    statusRequestId.value =
                        currentVisitorRequestId;
                }


                checkRequestStatus();

                startVisitorStatusRefresh();
            }
        }


        /*
         * EMPLOYEE PAGE
         */

        if (
            employeeWorkspace &&
            !employeeWorkspace.classList.contains(
                "hidden"
            )
        ) {

            if (loggedInEmployee) {

                loadEmployeeRequests();

                loadEmployeeHistory();

                startEmployeeAutoRefresh();
            }
        }
    }
);


/* =========================================================
   START APPLICATION
========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    async function() {

        restoreVisitorSession();


        /*
         * Load employee list immediately.
         * This ensures the visitor dropdown
         * contains the latest employees.
         */

        await loadEmployees();


        /*
         * Start from role selection.
         */

        showRoleSelection();
    }
);