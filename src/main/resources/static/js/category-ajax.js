document.addEventListener('DOMContentLoaded', () => {
    const apiUrl = `${contextPath}/api/category`;
    const tableBody = document.getElementById('category-table-body');
    const form = document.getElementById('category-form');
    const modal = document.getElementById('category-modal');
    const message = document.getElementById('category-message');
    const idInput = document.getElementById('category-id');
    const nameInput = document.getElementById('category-name');
    const iconInput = document.getElementById('category-icon');
    const keywordInput = document.getElementById('category-keyword');

    async function request(url, options = {}) {
        try {
            const ajaxOptions = {
                url,
                type: options.method || 'GET',
                dataType: 'json'
            };
            if (options.formData) {
                ajaxOptions.data = options.formData;
                ajaxOptions.contentType = false;
                ajaxOptions.processData = false;
            } else if (options.body) {
                ajaxOptions.data = options.body;
                ajaxOptions.contentType = 'application/json; charset=UTF-8';
                ajaxOptions.processData = false;
            }
            const payload = await $.ajax(ajaxOptions);
            if (!payload.status) {
                const firstFieldError = payload.body && typeof payload.body === 'object'
                    ? Object.values(payload.body)[0] : null;
                throw new Error(firstFieldError || payload.message || 'Không thể thực hiện yêu cầu.');
            }
            return payload;
        } catch (error) {
            if (error instanceof Error) throw error;
            const payload = error.responseJSON || {};
            const firstFieldError = payload.body && typeof payload.body === 'object'
                ? Object.values(payload.body)[0] : null;
            throw new Error(firstFieldError || payload.message || 'Không thể thực hiện yêu cầu.');
        }
    }

    function showMessage(text, type) {
        message.textContent = text;
        message.className = `alert ${type}`;
        message.hidden = false;
    }

    function createCell(text, className) {
        const cell = document.createElement('td');
        cell.textContent = text;
        if (className) cell.className = className;
        return cell;
    }

    function actionButton(label, className, handler) {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = `btn ${className}`;
        button.textContent = label;
        button.addEventListener('click', handler);
        return button;
    }

    function imageUrl(path, defaultFolder) {
        if (!path || !path.trim()) return null;
        const value = path.trim().replace(/\\/g, '/');
        if (/^https?:\/\//i.test(value)) return value;

        let relativePath = value.replace(/^\/+/, '');
        if (relativePath.startsWith('uploads/')) relativePath = relativePath.substring(8);
        if (relativePath.startsWith('images/')) relativePath = relativePath.substring(7);
        if (!relativePath.includes('/')) relativePath = `${defaultFolder}/${relativePath}`;
        if (relativePath.split('/').includes('..')) return null;

        return `${contextPath}/${relativePath.split('/').map(encodeURIComponent).join('/')}`;
    }

    function createImageCell(path, alt, defaultFolder) {
        const cell = document.createElement('td');
        const source = imageUrl(path, defaultFolder);
        const placeholder = document.createElement('span');
        placeholder.className = 'image-placeholder';
        placeholder.textContent = 'Chưa có ảnh';

        if (!source) {
            cell.appendChild(placeholder);
            return cell;
        }

        const image = document.createElement('img');
        image.className = 'table-image';
        image.src = source;
        image.alt = alt;
        image.loading = 'lazy';
        image.addEventListener('error', () => image.replaceWith(placeholder), {once: true});
        cell.appendChild(image);
        return cell;
    }

    function render(categories) {
        tableBody.replaceChildren();
        if (categories.length === 0) {
            const row = document.createElement('tr');
            const cell = createCell('Không tìm thấy danh mục phù hợp.', 'empty');
            cell.colSpan = 4;
            row.appendChild(cell);
            tableBody.appendChild(row);
            return;
        }
        categories.forEach(category => {
            const row = document.createElement('tr');
            row.appendChild(createCell(category.id));
            row.appendChild(createImageCell(category.icon, category.name, 'category'));
            const nameCell = createCell(category.name);
            const strong = document.createElement('strong');
            strong.textContent = nameCell.textContent;
            nameCell.replaceChildren(strong);
            row.appendChild(nameCell);

            const actionsCell = document.createElement('td');
            const actions = document.createElement('div');
            actions.className = 'action-group';
            actions.appendChild(actionButton('Sửa', 'btn-warning', () => openEdit(category.id)));
            actions.appendChild(actionButton('Xóa', 'btn-danger', () => remove(category.id, category.name)));
            actionsCell.appendChild(actions);
            row.appendChild(actionsCell);
            tableBody.appendChild(row);
        });
    }

    async function load() {
        try {
            const keyword = encodeURIComponent(keywordInput.value.trim());
            const payload = await request(`${apiUrl}?keyword=${keyword}`);
            render(payload.body);
        } catch (error) {
            render([]);
            showMessage(error.message, 'error');
        }
    }

    function openCreate() {
        form.reset();
        idInput.value = '';
        document.getElementById('category-modal-title').textContent = 'Thêm danh mục';
        modal.hidden = false;
        nameInput.focus();
    }

    async function openEdit(id) {
        try {
            const payload = await request(`${apiUrl}/${id}`);
            form.reset();
            idInput.value = payload.body.id;
            nameInput.value = payload.body.name;
            document.getElementById('category-modal-title').textContent = 'Cập nhật danh mục';
            modal.hidden = false;
            nameInput.focus();
        } catch (error) {
            showMessage(error.message, 'error');
        }
    }

    function closeModal() {
        modal.hidden = true;
    }

    async function save(event) {
        event.preventDefault();
        const id = idInput.value;
        const data = {
            name: nameInput.value.trim(),
            icon: null
        };
        const formData = new FormData();
        formData.append('data', new Blob([JSON.stringify(data)], {type: 'application/json'}));
        if (iconInput.files.length > 0) formData.append('image', iconInput.files[0]);
        try {
            const payload = await request(id ? `${apiUrl}/${id}` : apiUrl, {
                method: id ? 'PUT' : 'POST',
                formData
            });
            closeModal();
            showMessage(payload.message, 'success');
            await load();
        } catch (error) {
            showMessage(error.message, 'error');
        }
    }

    async function remove(id, name) {
        if (!window.confirm(`Bạn có chắc muốn xóa danh mục "${name}"?`)) return;
        try {
            const payload = await request(`${apiUrl}/${id}`, {method: 'DELETE'});
            showMessage(payload.message, 'success');
            await load();
        } catch (error) {
            showMessage(error.message, 'error');
        }
    }

    document.getElementById('add-category-button').addEventListener('click', openCreate);
    document.getElementById('category-modal-close').addEventListener('click', closeModal);
    document.getElementById('category-cancel-button').addEventListener('click', closeModal);
    document.getElementById('category-search-form').addEventListener('submit', event => {
        event.preventDefault();
        load();
    });
    document.getElementById('category-clear-button').addEventListener('click', () => {
        keywordInput.value = '';
        load();
    });
    modal.addEventListener('click', event => {
        if (event.target === modal) closeModal();
    });
    form.addEventListener('submit', save);
    load();
});
