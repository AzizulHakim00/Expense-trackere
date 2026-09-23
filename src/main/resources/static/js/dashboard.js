(() => {
  const $ = (id) => document.getElementById(id);
  const csrf = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
  const month = $('month'), week = $('week'), dialog = $('expense-dialog');
  const today = new Date();
  const localToday = new Date(today.getTime() - today.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
  month.value = localToday.slice(0, 7);
  week.value = localToday;
  let period = 'monthly', rows = [];
  const money = (value) => Number(value).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });

  function notice(text, error = false) {
    const node = $('message'); node.textContent = text; node.hidden = false; node.classList.toggle('error', error);
  }
  async function api(path, options = {}) {
    const response = await fetch(path, { ...options, credentials: 'same-origin', headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...(options.method && options.method !== 'GET' ? { [csrfHeader]: csrf } : {}),
    } });
    if (response.status === 401) { window.location.assign('/login'); throw new Error('Session expired'); }
    if (!response.ok) {
      const body = await response.json().catch(() => ({}));
      throw new Error(body.error || body.detail || `Request failed (${response.status})`);
    }
    return response.status === 204 ? null : response.json();
  }
  function text(tag, value, className = '') {
    const element = document.createElement(tag); element.textContent = value;
    if (className) element.className = className;
    return element;
  }
  function draw(summary) {
    $('total').textContent = money(summary.total);
    $('count').textContent = summary.count;
    $('range').textContent = `${summary.start} to ${new Date(`${summary.endExclusive}T00:00:00`).toLocaleDateString(undefined, { dateStyle: 'medium' })} (exclusive)`;
    const categories = Object.entries(summary.byCategory).sort((a, b) => Number(b[1]) - Number(a[1]));
    $('top-category').textContent = categories[0]?.[0] || '—';
    $('top-amount').textContent = categories.length ? `${money(categories[0][1])} spent` : 'No spending yet';
    const list = $('categories'); list.replaceChildren();
    categories.forEach(([name, amount]) => {
      const item = text('div', '', 'category-item');
      const heading = document.createElement('div'); heading.append(text('span', name), text('small', money(amount)));
      const meter = text('div', '', 'meter'); const bar = document.createElement('i');
      bar.style.width = `${summary.total > 0 ? (Number(amount) / Number(summary.total)) * 100 : 0}%`;
      meter.append(bar); item.append(heading, meter); list.append(item);
    });
    if (!categories.length) list.append(text('p', 'Your category breakdown will appear here.', 'muted'));
    const tbody = $('expense-rows'); tbody.replaceChildren(); $('empty').hidden = rows.length > 0;
    rows.forEach((expense) => {
      const tr = document.createElement('tr');
      const title = document.createElement('td'); title.textContent = expense.title;
      const category = document.createElement('td'); category.append(text('span', expense.category, 'category-tag'));
      const actions = text('td', '', 'actions');
      const edit = text('button', 'Edit'); edit.type = 'button'; edit.addEventListener('click', () => openForm(expense));
      const remove = text('button', 'Delete'); remove.type = 'button'; remove.addEventListener('click', () => deleteExpense(expense));
      actions.append(edit, remove);
      tr.append(title, category, text('td', expense.date), text('td', money(expense.amount)), actions);
      tbody.append(tr);
    });
  }
  async function refresh() {
    try {
      const key = period === 'monthly' ? `month=${month.value}` : `date=${week.value}`;
      const summary = await api(`/api/expenses/summary/${period}?${key}`);
      rows = await api(`/api/expenses?start=${summary.start}&end=${summary.endExclusive}`);
      draw(summary);
    } catch (err) { notice(err.message, true); }
  }
  function openForm(expense = null) {
    $('expense-form').reset(); $('expense-id').value = expense?.id || '';
    $('title').value = expense?.title || '';
    $('amount').value = expense?.amount || '';
    $('date').value = expense?.date || localToday;
    $('category').value = expense?.category || 'Food';
    $('note').value = expense?.note || '';
    $('dialog-title').textContent = expense ? 'Edit expense' : 'Add expense'; dialog.showModal();
  }
  async function deleteExpense(expense) {
    if (!window.confirm(`Delete “${expense.title}”?`)) return;
    try { await api(`/api/expenses/${encodeURIComponent(expense.id)}`, { method: 'DELETE' });
      notice('Expense deleted.'); await refresh(); }
    catch (err) { notice(err.message, true); }
  }
  $('expense-form').addEventListener('submit', async (event) => {
    event.preventDefault(); const id = $('expense-id').value;
    const body = JSON.stringify({ title: $('title').value, amount: Number($('amount').value), category: $('category').value,
      date: $('date').value, note: $('note').value });
    const button = $('expense-form').querySelector('button[type="submit"]'); button.disabled = true;
    try { await api(id ? `/api/expenses/${encodeURIComponent(id)}` : '/api/expenses', { method: id ? 'PUT' : 'POST', body });
      dialog.close(); notice(id ? 'Expense updated.' : 'Expense added.'); await refresh(); }
    catch (err) { notice(err.message, true); }
    finally { button.disabled = false; }
  });
  $('add-button').addEventListener('click', () => openForm());
  $('close-dialog').addEventListener('click', () => dialog.close());
  document.querySelectorAll('[data-period]').forEach((button) => button.addEventListener('click', () => {
    period = button.dataset.period;
    document.querySelectorAll('[data-period]').forEach((b) => b.classList.toggle('selected', b === button));
    $('month-label').hidden = period !== 'monthly'; $('week-label').hidden = period !== 'weekly'; refresh();
  }));
  month.addEventListener('change', refresh); week.addEventListener('change', refresh); refresh();
})();
