const modal = document.getElementById('panne-modal');
const openBtn = document.getElementById('open-panne-modal');
const closeBtn = document.getElementById('close-modal-icon');
const cancelModalBtn = document.getElementById('cancel-modal-btn');
const backdrop = document.getElementById('modal-backdrop');

function openModal() {
    if (modal) modal.classList.remove('hidden');
}

function closeModal() {
    if (modal) modal.classList.add('hidden');
}

if (openBtn) openBtn.addEventListener('click', openModal);
if (closeBtn) closeBtn.addEventListener('click', closeModal);
if (cancelModalBtn) cancelModalBtn.addEventListener('click', closeModal);
if (backdrop) backdrop.addEventListener('click', closeModal);
