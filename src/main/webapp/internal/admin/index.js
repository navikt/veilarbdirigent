function render() {
    fetch('/veilarbdirigent/api/admin/status')
        .then((resp) => resp.json())
        .then(prepStatusResponse)
        .then(createStatusHtml);


    fetch('/veilarbdirigent/api/admin')
        .then((resp) => resp.json())
        .then(createFeiledTasks);
}

function prepStatusResponse(statuses) {
    const expectedStatuses = { PENDING: 0, WORKING: 0, OK: 0, FAILED: 0 };
    return Object.assign({}, expectedStatuses, statuses);
}

function element(type, attrs, content) {
    const element = document.createElement(type);

    Object.entries(attrs)
        .filter(([attr]) => !attr.startsWith('on'))
        .forEach(([attr, value]) => element.setAttribute(attr, value));

    Object.entries(attrs)
        .filter(([attr]) => attr.startsWith('on'))
        .forEach(([attr, value]) => {
            element[attr] = value
        });

    element.innerHTML = content;

    return element;
}

const span = (content, cls) => element('span', { 'class': cls }, content );
const button = (content, attrs) => element('button', attrs, content );
const head = (content) => span(`<b>${content}</b>`, 'head');
const cell = (content) => span(`${content}`, 'cell');

function createStatusHtml(statuses) {
    const container = document.querySelector('.status');
    container.innerHTML = '';

    container.appendChild(head('Status'));
    container.appendChild(head('Count'));

    Object.entries(statuses)
        .forEach(([status, count]) => {
            container.appendChild(cell(status));
            container.appendChild(cell(count));
        })

}

function createFeiledTasks(tasks) {
    const container = document.querySelector('.failed');
    container.innerHTML = '';

    container.appendChild(head('Run'));
    container.appendChild(head('Id'));
    container.appendChild(head('Type'));
    container.appendChild(head('Status'));
    container.appendChild(head('Created'));
    container.appendChild(head('Attempts'));
    container.appendChild(head('Next'));
    container.appendChild(head('Error'));

    tasks.forEach((task) => {
        container.appendChild(button('Run', { onclick: rerun(task.id) }));
        container.appendChild(cell(`${task.id}`));
        container.appendChild(cell(`${task.type.type}`));
        container.appendChild(cell(`${task.status}`));
        container.appendChild(cell(`${task.created}`));
        container.appendChild(cell(`${task.attempts}`));
        container.appendChild(cell(`${task.nextAttempt}`));
        container.appendChild(cell(`${task.error}`));
    });
}

function rerun(taskid) {
    return () => {
        fetch(`/veilarbdirigent/api/admin/task/${taskid}/rerun`, { method: 'POST' })
            .then(render);
    };
}

function forcerun() {
    fetch(`/veilarbdirigent/api/admin/forcerun`, { method: 'POST' })
        .then(render);
}

const force = document.querySelector('.force');
force.appendChild(button('Force run', { onclick: forcerun }));
render();