let mainCounter = 0;
let subCounter = 0;

function log(record) {
    const isMain = record.type === "request" || record.type === "response";

    let elementId, label;
    if (isMain) {
        mainCounter++;
        subCounter = 0;
        elementId = `record-${mainCounter}`;
        label = mainCounter.toString().padStart(4, '0');
    } else {
        subCounter++;
        elementId = `record-${mainCounter}.${subCounter.toString().padStart(4, '0')}`;
        label = `${mainCounter.toString().padStart(4, '0')}.${subCounter.toString().padStart(4, '0')}`;
    }

    const element = document.createElement("div");
    element.id = elementId;
    document.getElementById("tree").append(element);
    jsonview.render(jsonview.create(record), element);
    updateRecordLabel(element.id, label);
}

function clear() {
    document.getElementById("tree").replaceChildren();
    mainCounter = 0;
    subCounter = 0;
}

function updateRecordLabel(recordId, newLabel) {
    const element = document.getElementById(recordId);
    if (!element) return;
    const keyElement = element.querySelector('.json-container .line .json-key');
    if (keyElement) {
        keyElement.textContent = newLabel;
    }
}

function highlight(divToHighlight) {
    document.querySelectorAll('[id^="record-"]').forEach(div => {
        div.classList.remove("highlighted");
    });

    divToHighlight.classList.add("highlighted");
    divToHighlight.scrollIntoView({behavior: 'smooth', block: 'start'});
}

function onClickSetup() {
    const treeContainer = document.getElementById("tree");

    treeContainer.addEventListener("click", function(event) {
        const recordDiv = event.target.closest('[id^="record-"]');

        if (recordDiv) {
            highlight(recordDiv);
            if (window.mainController) {
                const params = new URLSearchParams(window.location.search);
                const role = params.get('role');
                window.mainController.onLogClick(role, recordDiv.id);
            }
        }
    });
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', onClickSetup);
} else {
    onClickSetup();
}
