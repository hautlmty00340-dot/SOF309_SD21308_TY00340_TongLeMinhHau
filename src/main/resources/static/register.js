(() => {
  const main = document.querySelector('main');
  const loginMarkup = main.innerHTML;
  const loginTitle = 'Đăng nhập | ShopPulse';
  const homeTitle = 'ShopPulse — Mua sắm vui mỗi ngày';
  let currentRoute = null;

  const registerMarkup = `
    <div class="register-page">
      <section class="register-intro" aria-labelledby="registerWelcome">
        <div class="register-badge">
          <span class="material-symbols-outlined" aria-hidden="true">celebration</span>
          <span>Thành viên mới, ngàn ưu đãi mới</span>
        </div>
        <h1 class="register-heading" id="registerWelcome">
          Tạo tài khoản –
          <span>Mở khóa niềm vui.</span>
        </h1>
        <p class="register-description">
          Đăng ký ShopPulse để lưu sản phẩm yêu thích, theo dõi đơn hàng và nhận ưu đãi dành riêng cho bạn.
        </p>

        <div class="register-promo">
          <img
            class="register-promo-image"
            src="https://lh3.googleusercontent.com/aida-public/AB6AXuBK3bl6nJ8qPLvmwtRGLbmS-uO7zNGFDtXd4nHIKps6KWuTeBT6tGFbUIk4VrGeHRJtri0xc4C9r-ZcVS3tjP2XWZuQT46nf-MnHbctYtnf9Xoz6xU1c711wO6InGVJrTn43bNRevgHRTFJZedOzc_sAPQw_5OH1tijA68rD8A0m9ZJ_VBeqpd7pe5bTkx9fypsHAtjChotJBTOkKzHH4992MKQH1KIhZqlJDo_qCcf"
            alt="Ưu đãi mua sắm ShopPulse"
          />
          <div class="register-promo-card">
            <span class="material-symbols-outlined" aria-hidden="true">redeem</span>
            <div>
              <span class="register-promo-label">Ưu đãi thành viên</span>
              <strong>Bắt đầu với voucher 50K</strong>
            </div>
          </div>
        </div>

        <div class="register-benefits">
          <div class="register-benefit">
            <span class="material-symbols-outlined" aria-hidden="true">verified</span>
            <div><strong>500.000+</strong><span>Khách hàng tin tưởng</span></div>
          </div>
          <div class="register-benefit">
            <span class="material-symbols-outlined" aria-hidden="true">published_with_changes</span>
            <div><strong>30 ngày</strong><span>Đổi trả dễ dàng</span></div>
          </div>
          <div class="register-benefit">
            <span class="material-symbols-outlined" aria-hidden="true">security</span>
            <div><strong>Bảo mật</strong><span>Thông tin an toàn</span></div>
          </div>
        </div>
      </section>

      <section class="register-card" aria-labelledby="registerTitle">
        <div class="register-card-heading">
          <h2 id="registerTitle">Tạo tài khoản mới</h2>
          <p>Tham gia ShopPulse chỉ với vài thông tin.</p>
        </div>

        <form class="register-form" id="registerForm" novalidate>
          <div class="register-field">
            <label for="fullName">Họ và tên</label>
            <div class="register-input-wrap">
              <span class="material-symbols-outlined" aria-hidden="true">person</span>
              <input id="fullName" name="fullName" type="text" placeholder="Nguyễn Văn An" autocomplete="name" aria-describedby="fullNameError"/>
            </div>
            <p class="register-error" id="fullNameError" aria-live="polite"></p>
          </div>

          <div class="register-field">
            <label for="contact">Email hoặc số điện thoại</label>
            <div class="register-input-wrap">
              <span class="material-symbols-outlined" aria-hidden="true">mail</span>
              <input id="contact" name="contact" type="text" placeholder="example@gmail.com hoặc 0912345678" autocomplete="username" aria-describedby="contactError"/>
            </div>
            <p class="register-error" id="contactError" aria-live="polite"></p>
          </div>

          <div class="register-field">
            <label for="password">Mật khẩu</label>
            <div class="register-input-wrap">
              <span class="material-symbols-outlined" aria-hidden="true">lock</span>
              <input id="password" name="password" type="password" placeholder="Tối thiểu 8 ký tự" autocomplete="new-password" aria-describedby="passwordError"/>
              <button class="register-password-toggle" type="button" data-toggle-password="password" aria-label="Hiện mật khẩu">
                <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
              </button>
            </div>
            <p class="register-error" id="passwordError" aria-live="polite"></p>
          </div>

          <div class="register-field">
            <label for="confirmPassword">Xác nhận mật khẩu</label>
            <div class="register-input-wrap">
              <span class="material-symbols-outlined" aria-hidden="true">lock_reset</span>
              <input id="confirmPassword" name="confirmPassword" type="password" placeholder="Nhập lại mật khẩu" autocomplete="new-password" aria-describedby="confirmPasswordError"/>
              <button class="register-password-toggle" type="button" data-toggle-password="confirmPassword" aria-label="Hiện mật khẩu">
                <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
              </button>
            </div>
            <p class="register-error" id="confirmPasswordError" aria-live="polite"></p>
          </div>

          <div class="register-terms">
            <label>
              <input id="acceptTerms" name="acceptTerms" type="checkbox"/>
              <span>Tôi đồng ý với Điều khoản dịch vụ và Chính sách bảo mật.</span>
            </label>
            <p class="register-error" id="termsError" aria-live="polite"></p>
          </div>

          <button class="register-submit" id="registerButton" type="submit">
            <span class="register-spinner" id="registerSpinner" aria-hidden="true"></span>
            <span id="registerButtonText">Tạo tài khoản</span>
          </button>
        </form>

        <div class="register-card-footer">
          <p>Đã có tài khoản? <a href="#login">Đăng nhập</a></p>
          <span><span class="material-symbols-outlined" aria-hidden="true">lock</span>Thông tin được bảo vệ an toàn</span>
        </div>
      </section>

      <div class="register-toast-region" id="registerToastRegion" aria-live="polite"></div>
    </div>
  `;

  function showToast() {
    const region = document.getElementById('registerToastRegion');
    const toast = document.createElement('div');
    toast.className = 'register-toast';
    toast.innerHTML = `
      <span class="material-symbols-outlined" aria-hidden="true">info</span>
      <div>
        <strong>Biểu mẫu hợp lệ</strong>
        <p>Trang đăng ký chưa kết nối máy chủ; thông tin chưa được gửi hoặc lưu.</p>
      </div>
    `;
    region.appendChild(toast);
    window.setTimeout(() => toast.remove(), 5000);
  }

  function initializeRegisterForm() {
    const form = document.getElementById('registerForm');
    const fullName = document.getElementById('fullName');
    const contact = document.getElementById('contact');
    const password = document.getElementById('password');
    const confirmPassword = document.getElementById('confirmPassword');
    const acceptTerms = document.getElementById('acceptTerms');
    const registerButton = document.getElementById('registerButton');
    const registerSpinner = document.getElementById('registerSpinner');
    const registerButtonText = document.getElementById('registerButtonText');
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const phonePattern = /^(0|\+84)(3|5|7|8|9)[0-9]{8}$/;

    function setError(input, message) {
      const error = document.getElementById(`${input.id}Error`);
      error.textContent = message;
      error.classList.add('is-visible');
      input.classList.add('has-error');
      input.setAttribute('aria-invalid', 'true');
      return false;
    }

    function clearError(input) {
      const error = document.getElementById(`${input.id}Error`);
      error.textContent = '';
      error.classList.remove('is-visible');
      input.classList.remove('has-error');
      input.removeAttribute('aria-invalid');
      return true;
    }

    function validateName() {
      return fullName.value.trim().length < 2
        ? setError(fullName, 'Vui lòng nhập họ tên có ít nhất 2 ký tự.')
        : clearError(fullName);
    }

    function validateContact() {
      const value = contact.value.trim();
      return !emailPattern.test(value) && !phonePattern.test(value)
        ? setError(contact, 'Vui lòng nhập email hợp lệ hoặc số điện thoại Việt Nam.')
        : clearError(contact);
    }

    function validatePassword() {
      if (password.value.length < 8) {
        return setError(password, 'Mật khẩu phải có ít nhất 8 ký tự.');
      }
      clearError(password);
      if (confirmPassword.value) validateConfirmPassword();
      return true;
    }

    function validateConfirmPassword() {
      return !confirmPassword.value || confirmPassword.value !== password.value
        ? setError(confirmPassword, 'Mật khẩu xác nhận chưa khớp.')
        : clearError(confirmPassword);
    }

    function validateTerms() {
      const error = document.getElementById('termsError');
      if (!acceptTerms.checked) {
        error.textContent = 'Vui lòng đồng ý với điều khoản để tiếp tục.';
        error.classList.add('is-visible');
        acceptTerms.setAttribute('aria-invalid', 'true');
        return false;
      }
      error.textContent = '';
      error.classList.remove('is-visible');
      acceptTerms.removeAttribute('aria-invalid');
      return true;
    }

    fullName.addEventListener('input', validateName);
    contact.addEventListener('input', validateContact);
    password.addEventListener('input', validatePassword);
    confirmPassword.addEventListener('input', validateConfirmPassword);
    acceptTerms.addEventListener('change', validateTerms);

    document.querySelectorAll('[data-toggle-password]').forEach((button) => {
      button.addEventListener('click', () => {
        const input = document.getElementById(button.dataset.togglePassword);
        const showingPassword = input.type === 'password';
        input.type = showingPassword ? 'text' : 'password';
        button.setAttribute('aria-label', showingPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu');
        button.querySelector('span').textContent = showingPassword ? 'visibility_off' : 'visibility';
      });
    });

    form.addEventListener('submit', (event) => {
      event.preventDefault();
      const isValid = [
        validateName(),
        validateContact(),
        validatePassword(),
        validateConfirmPassword(),
        validateTerms()
      ].every(Boolean);

      if (!isValid) return;

      registerButton.disabled = true;
      registerSpinner.classList.add('is-visible');
      registerButtonText.textContent = 'Đang kiểm tra...';

      window.setTimeout(() => {
        registerButton.disabled = false;
        registerSpinner.classList.remove('is-visible');
        registerButtonText.textContent = 'Tạo tài khoản';
        showToast();
      }, 500);
    });
  }

  function renderRoute() {
    const requestedRoute = window.location.hash.slice(1);
    const route = ['home', 'login', 'register'].includes(requestedRoute)
      ? requestedRoute
      : 'home';
    if (route === currentRoute) return;

    if (route === 'home') {
      main.innerHTML = window.getShopHomeMarkup();
      document.title = homeTitle;
      window.initializeShopHome();
    } else if (route === 'register') {
      main.innerHTML = registerMarkup;
      document.title = 'Tạo tài khoản | ShopPulse';
      initializeRegisterForm();
    } else {
      main.innerHTML = loginMarkup;
      document.title = loginTitle;
      window.initializeLoginPage();
    }

    currentRoute = route;
  }

  window.addEventListener('hashchange', renderRoute);
  renderRoute();
})();
