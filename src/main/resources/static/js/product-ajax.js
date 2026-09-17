document.addEventListener('DOMContentLoaded', () => {
    const productApiUrl = `${contextPath}/api/product`;
    const categoryApiUrl = `${contextPath}/api/category`;
    const tableBody = document.getElementById('product-table-body');
    const form = document.getElementById('product-form');
    const modal = document.getElementById('product-modal');
    const message = document.getElementById('product-message');
    const keywordInput = document.getElementById('product-keyword');

    const fields = {
        id: document.getElementById('product-id'),
        name: document.getElementById('product-name'),
        categoryId: document.getElementById('product-category'),
        price: document.getElementById('product-price'),
        quantity: document.getElementById('product-quantity'),
        imageFile: document.getElementById('product-image'),
        description: document.getElementById('product-description')
    };

    async function request(options) {
        try {
            const payload = await $.ajax(options);
            if (!payload.status) {
                throw new Error(payload.message || 'Không thể thực hiện yêu cầu.');
            }
            return payload;
        } catch (error) {
            if (error instanceof Error) {
                throw error;
            }

            const payload = error.responseJSON || {};
            throw new Error(payload.message || 'Không thể thực hiện yêu cầu.');
        }
    }

    function showMessage(text, type) {
        message.textContent = text;
        message.className = `alert ${type}`;
        message.hidden = false;
    }

    function createCell(text) {
        const cell = document.createElement('td');
        cell.textContent = text;
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
        if (!path || !path.trim()) {
            return null;
        }

        const value = path.trim().replace(/\\/g, '/');
        if (/^https?:\/\//i.test(value)) {
            return value;
        }

        let relativePath = value.replace(/^\/+/, '');
        if (relativePath.startsWith('uploads/')) {
            relativePath = relativePath.substring(8);
        }
        if (relativePath.startsWith('images/')) {
            relativePath = relativePath.substring(7);
        }
        if (!relativePath.includes('/')) {
            relativePath = `${defaultFolder}/${relativePath}`;
        }
        if (relativePath.split('/').includes('..')) {
            return null;
        }

        const encodedPath = relativePath
                .split('/')
                .map(encodeURIComponent)
                .join('/');
        return `${contextPath}/${encodedPath}`;
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
        image.addEventListener(
                'error',
                () => image.replaceWith(placeholder),
                {once: true});
        cell.appendChild(image);
        return cell;
    }

    function formatCurrency(value) {
        return new Intl.NumberFormat(
                'vi-VN',
                {
                    style: 'currency',
                    currency: 'VND'
                })
                .format(Number(value));
    }

    function render(products) {
        tableBody.replaceChildren();

        if (products.length === 0) {
            const row = document.createElement('tr');
            const cell = createCell('Không tìm thấy sản phẩm phù hợp.');
            cell.colSpan = 7;
            cell.className = 'empty';
            row.appendChild(cell);
            tableBody.appendChild(row);
            return;
        }

        products.forEach(product => {
            const row = document.createElement('tr');
            row.appendChild(createCell(product.id));
            row.appendChild(
                    createImageCell(product.image, product.name, 'product'));

            const nameCell = createCell(product.name);
            const strong = document.createElement('strong');
            strong.textContent = product.name;
            nameCell.replaceChildren(strong);
            row.appendChild(nameCell);

            const categoryName = product.category
                    ? product.category.name
                    : '';
            row.appendChild(createCell(categoryName));
            row.appendChild(createCell(formatCurrency(product.price)));
            row.appendChild(createCell(product.quantity));

            const actionsCell = document.createElement('td');
            const actions = document.createElement('div');
            actions.className = 'action-group';
            actions.appendChild(
                    actionButton(
                            'Sửa',
                            'btn-warning',
                            () => openEdit(product.id)));
            actions.appendChild(
                    actionButton(
                            'Xóa',
                            'btn-danger',
                            () => remove(product.id, product.name)));
            actionsCell.appendChild(actions);
            row.appendChild(actionsCell);
            tableBody.appendChild(row);
        });
    }

    async function loadProducts() {
        try {
            const keyword = keywordInput.value.trim();
            const payload = await request({
                url: productApiUrl,
                type: 'GET',
                dataType: 'json',
                data: {
                    keyword: keyword
                }
            });
            render(payload.body);
        } catch (error) {
            render([]);
            showMessage(error.message, 'error');
        }
    }

    async function loadCategories(selectedId) {
        const payload = await request({
            url: categoryApiUrl,
            type: 'GET',
            dataType: 'json'
        });

        fields.categoryId.replaceChildren();

        const placeholder = document.createElement('option');
        placeholder.value = '';
        placeholder.textContent = '-- Chọn danh mục --';
        fields.categoryId.appendChild(placeholder);

        payload.body.forEach(category => {
            const option = document.createElement('option');
            option.value = category.id;
            option.textContent = category.name;
            option.selected = Number(selectedId) === Number(category.id);
            fields.categoryId.appendChild(option);
        });
    }

    async function openCreate() {
        form.reset();
        fields.id.value = '';
        document.getElementById('product-modal-title').textContent = 'Thêm sản phẩm';

        try {
            await loadCategories();
            modal.hidden = false;
            fields.name.focus();
        } catch (error) {
            showMessage(error.message, 'error');
        }
    }

    async function openEdit(id) {
        try {
            const payload = await request({
                url: `${productApiUrl}/getProduct`,
                type: 'POST',
                dataType: 'json',
                data: {
                    id: id
                }
            });

            const product = payload.body;
            form.reset();

            const categoryId = product.category
                    ? product.category.id
                    : null;
            await loadCategories(categoryId);

            fields.id.value = product.id;
            fields.name.value = product.name;
            fields.price.value = product.price;
            fields.quantity.value = product.quantity;
            fields.description.value = product.description || '';
            document.getElementById('product-modal-title').textContent =
                    'Cập nhật sản phẩm';
            modal.hidden = false;
            fields.name.focus();
        } catch (error) {
            showMessage(error.message, 'error');
        }
    }

    function closeModal() {
        modal.hidden = true;
    }

    async function save(event) {
        event.preventDefault();

        const id = fields.id.value;
        const formData = new FormData(form);
        const url = id
                ? `${productApiUrl}/updateProduct`
                : `${productApiUrl}/addProduct`;
        const method = id ? 'PUT' : 'POST';

        try {
            const payload = await request({
                url: url,
                type: method,
                dataType: 'json',
                data: formData,
                cache: false,
                contentType: false,
                processData: false
            });

            closeModal();
            showMessage(payload.message, 'success');
            await loadProducts();
        } catch (error) {
            showMessage(error.message, 'error');
        }
    }

    async function remove(id, name) {
        const confirmed = window.confirm(
                `Bạn có chắc muốn xóa sản phẩm "${name}"?`);
        if (!confirmed) {
            return;
        }

        try {
            const payload = await request({
                url: `${productApiUrl}/deleteProduct?productId=${id}`,
                type: 'DELETE',
                dataType: 'json'
            });
            showMessage(payload.message, 'success');
            await loadProducts();
        } catch (error) {
            showMessage(error.message, 'error');
        }
    }

    document.getElementById('add-product-button')
            .addEventListener('click', openCreate);
    document.getElementById('product-modal-close')
            .addEventListener('click', closeModal);
    document.getElementById('product-cancel-button')
            .addEventListener('click', closeModal);

    document.getElementById('product-search-form')
            .addEventListener('submit', event => {
                event.preventDefault();
                loadProducts();
            });

    document.getElementById('product-clear-button')
            .addEventListener('click', () => {
                keywordInput.value = '';
                loadProducts();
            });

    modal.addEventListener('click', event => {
        if (event.target === modal) {
            closeModal();
        }
    });

    form.addEventListener('submit', save);
    loadProducts();
});
