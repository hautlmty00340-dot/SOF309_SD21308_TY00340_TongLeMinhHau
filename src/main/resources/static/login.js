function initializeLoginPage() {
      // Input & Elements
      const identifierInput = document.getElementById('identifier');
      const identifierError = document.getElementById('identifierError');
      const identifierErrorText = document.getElementById('identifierErrorText');

      const passwordInput = document.getElementById('password');
      const passwordError = document.getElementById('passwordError');
      const passwordErrorText = document.getElementById('passwordErrorText');
      const togglePasswordBtn = document.getElementById('togglePasswordBtn');
      const passwordEyeIcon = document.getElementById('passwordEyeIcon');

      const loginForm = document.getElementById('loginForm');
      const submitBtn = document.getElementById('submitBtn');
      const submitSpinner = document.getElementById('submitSpinner');
      const submitBtnText = document.getElementById('submitBtnText');

      // Modal Elements
      const forgotModal = document.getElementById('forgotModal');
      const openForgotModalBtn = document.getElementById('openForgotModalBtn');
      const closeModalBtn = document.getElementById('closeModalBtn');
      const modalBackdrop = document.getElementById('modalBackdrop');
      const backToLoginBtn = document.getElementById('backToLoginBtn');
      const forgotForm = document.getElementById('forgotForm');
      const resetEmail = document.getElementById('resetEmail');
      const resetEmailError = document.getElementById('resetEmailError');
      const forgotSubmitBtn = document.getElementById('forgotSubmitBtn');
      const forgotSpinner = document.getElementById('forgotSpinner');
      const forgotSubmitText = document.getElementById('forgotSubmitText');
      const forgotSuccessBox = document.getElementById('forgotSuccessBox');

      if (!loginForm || loginForm.dataset.initialized) return;
      loginForm.dataset.initialized = 'true';

      // Toast function
      function showToast(title, message, isSuccess = true) {
        const toast = document.createElement('div');
        toast.className = `pointer-events-auto flex items-start gap-3 p-4 rounded-xl bg-surface-container-lowest shadow-xl border-l-4 ${isSuccess ? 'border-primary' : 'border-error'} transform translate-y-2 transition-all duration-300 max-w-sm`;
        
        toast.innerHTML = `
          <span class="material-symbols-outlined ${isSuccess ? 'text-primary' : 'text-error'} text-[24px]">
            ${isSuccess ? 'task_alt' : 'warning'}
          </span>
          <div class="flex-1">
            <h4 class="font-headline-sm text-headline-sm text-on-surface">${title}</h4>
            <p class="font-body-sm text-body-sm text-on-surface-variant mt-0.5">${message}</p>
          </div>
        `;

        const container = document.getElementById('toastContainer');
        container.appendChild(toast);

        setTimeout(() => {
          toast.classList.add('opacity-0', '-translate-y-2');
          setTimeout(() => toast.remove(), 300);
        }, 3500);
      }

      // Password visibility toggle
      togglePasswordBtn.addEventListener('click', () => {
        const isPassword = passwordInput.getAttribute('type') === 'password';
        passwordInput.setAttribute('type', isPassword ? 'text' : 'password');
        passwordEyeIcon.textContent = isPassword ? 'visibility_off' : 'visibility';
      });

      // Regex validation patterns
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      const phoneRegex = /^(0|\+84)(3|5|7|8|9)[0-9]{8}$/;

      function validateIdentifier(val) {
        const cleanVal = val.trim();
        if (!cleanVal) {
          identifierErrorText.textContent = 'Vui lòng nhập email hoặc số điện thoại.';
          identifierError.classList.remove('hidden');
          identifierError.classList.add('flex');
          identifierInput.classList.add('ring-error');
          return false;
        }

        const isEmail = emailRegex.test(cleanVal);
        const isPhone = phoneRegex.test(cleanVal);

        if (!isEmail && !isPhone) {
          identifierErrorText.textContent = 'Email chưa đúng định dạng hoặc số điện thoại chưa chuẩn 10 số.';
          identifierError.classList.remove('hidden');
          identifierError.classList.add('flex');
          identifierInput.classList.add('ring-error');
          return false;
        }

        identifierError.classList.add('hidden');
        identifierError.classList.remove('flex');
        identifierInput.classList.remove('ring-error');
        return true;
      }

      function validatePassword(val) {
        if (!val || val.length < 8) {
          passwordErrorText.textContent = 'Mật khẩu phải chứa ít nhất 8 ký tự.';
          passwordError.classList.remove('hidden');
          passwordError.classList.add('flex');
          passwordInput.classList.add('ring-error');
          return false;
        }
        passwordError.classList.add('hidden');
        passwordError.classList.remove('flex');
        passwordInput.classList.remove('ring-error');
        return true;
      }

      // Live validation clearing
      identifierInput.addEventListener('input', () => {
        if (!identifierError.classList.contains('hidden')) {
          validateIdentifier(identifierInput.value);
        }
      });

      passwordInput.addEventListener('input', () => {
        if (!passwordError.classList.contains('hidden')) {
          validatePassword(passwordInput.value);
        }
      });

      // Login form submission
      loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const isIdentValid = validateIdentifier(identifierInput.value);
        const isPassValid = validatePassword(passwordInput.value);

        if (!isIdentValid || !isPassValid) {
          return;
        }

        // Throttle & loading state
        submitBtn.disabled = true;
        submitSpinner.classList.remove('hidden');
        submitBtnText.textContent = 'Đang xác thực...';

        try {
          const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ identifier: identifierInput.value.trim(), password: passwordInput.value })
          });
          if (!response.ok) {
            const problem = await response.json().catch(() => ({}));
            throw new Error(problem.message || problem.detail || 'Thông tin đăng nhập không chính xác.');
          }
          showToast('Đăng nhập thành công!', 'Đang chuyển hướng đến ShopPulse.');
          window.location.hash = 'home';
        } catch (error) {
          showToast('Không thể đăng nhập', error.message, false);
        } finally {
          submitBtn.disabled = false;
          submitSpinner.classList.add('hidden');
          submitBtnText.textContent = 'Đăng nhập';
        }
      });

      // Modal Controls
      function openModal() {
        forgotModal.classList.remove('hidden');
        resetEmail.value = '';
        resetEmailError.classList.add('hidden');
        resetEmailError.classList.remove('flex');
        resetEmail.classList.remove('ring-error');
        forgotSuccessBox.classList.add('hidden');
        forgotSubmitBtn.disabled = false;
        forgotSpinner.classList.add('hidden');
        forgotSubmitText.textContent = 'Gửi mã xác nhận / Đặt lại';
      }

      function closeModal() {
        forgotModal.classList.add('hidden');
      }

      openForgotModalBtn.addEventListener('click', openModal);
      closeModalBtn.addEventListener('click', closeModal);
      modalBackdrop.addEventListener('click', closeModal);
      backToLoginBtn.addEventListener('click', closeModal);

      // Forgot password submit
      forgotForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const email = resetEmail.value.trim();

        if (!emailRegex.test(email)) {
          resetEmailError.classList.remove('hidden');
          resetEmailError.classList.add('flex');
          resetEmail.classList.add('ring-error');
          return;
        }

        resetEmailError.classList.add('hidden');
        resetEmailError.classList.remove('flex');
        resetEmail.classList.remove('ring-error');

        forgotSubmitBtn.disabled = true;
        forgotSpinner.classList.remove('hidden');
        forgotSubmitText.textContent = 'Đang xử lý...';

        setTimeout(() => {
          forgotSpinner.classList.add('hidden');
          forgotSubmitText.textContent = 'Đã gửi yêu cầu';
          forgotSuccessBox.classList.remove('hidden');
          showToast('Yêu cầu đã được tiếp nhận', 'Vui lòng kiểm tra email của bạn để tiếp tục.');
        }, 1000);
      });

      // Social Login mock handler
      window.handleSocialLogin = function(provider) {
        showToast(`Đang kết nối ${provider}`, 'Cổng xác thực an toàn đang khởi tạo...');
      };
}

window.initializeLoginPage = initializeLoginPage;

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initializeLoginPage, { once: true });
} else {
  initializeLoginPage();
}
