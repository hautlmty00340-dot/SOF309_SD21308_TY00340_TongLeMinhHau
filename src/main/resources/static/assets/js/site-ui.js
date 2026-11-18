(() => {
  'use strict';
  const styles = {
    forest: 'linear-gradient(135deg,#17b35a,#087b47)',
    ocean: 'linear-gradient(135deg,#3498ff,#123e98)',
    violet: 'linear-gradient(135deg,#a77bff,#6130b5)',
    sunset: 'linear-gradient(135deg,#ffaf5b,#d94367)',
    graphite: 'linear-gradient(135deg,#68738b,#22293a)'
  };
  const root = document.documentElement;
  function installCriticalMenuStyles() {
    if (document.getElementById('au-critical-menu-css')) return;
    const css = document.createElement('style');
    css.id = 'au-critical-menu-css';
    css.textContent = `
      .au-navbar .account-menu-wrap{position:relative;display:flex;align-items:center;flex:0 0 auto;min-width:42px}
      .au-navbar .account-popover[hidden]{display:none!important}
      .au-navbar .account-popover:not([hidden]){display:block;position:absolute;right:0;top:calc(100% + 12px);width:290px;max-width:calc(100vw - 24px);z-index:2500;background:var(--white,#fff);color:var(--gray-900,#111827);border:1px solid var(--gray-200,#e5e7eb);border-radius:16px;padding:9px;box-shadow:0 20px 45px #10182733}
      .au-navbar .account-popover-head{padding:14px;display:flex;flex-direction:column;gap:3px}
      .au-navbar .account-popover>a,.au-navbar .account-popover>button{display:flex;align-items:center;gap:10px;width:100%;padding:11px 12px;border-radius:8px;background:transparent;color:inherit;border:0;text-decoration:none;font-size:13px;text-align:left;cursor:pointer}
      .au-navbar .account-popover>a:hover,.au-navbar .account-popover>button:hover{background:var(--primary-light,#dcfce7)}
    `;
    document.head.appendChild(css);
  }
  const theme = localStorage.getItem('au_theme');
  root.dataset.theme = theme === 'dark' ? 'dark' : 'light';
  function setTheme(value) {
    root.dataset.theme = value;
    localStorage.setItem('au_theme', value);
    document.querySelectorAll('[data-theme-toggle]').forEach(el => {
      el.textContent = value === 'dark' ? '☀️ Chế độ sáng' : '🌙 Chế độ tối';
    });
  }
  function paintAvatar(el, user) {
    el.style.background = styles[user.avatar_style] || styles.forest;
    el.style.backgroundSize = 'cover';
    el.style.backgroundPosition = 'center';
    el.textContent = user.avatar_image ? '' : (user.fullname || user.username || 'K').charAt(0).toUpperCase();
    if (user.avatar_image && /^\/uploads\/avatars\/[a-f0-9-]+\.(png|jpg|webp)$/.test(user.avatar_image)) {
      el.style.backgroundImage = `url("${user.avatar_image}")`;
    }
  }
  async function logout() {
    try { await fetch('/api/auth/logout', { method: 'POST' }); } catch (_) {}
    sessionStorage.removeItem('au_user');
    location.href = '/index.html';
  }
  function accountMenu(anchor, user) {
    const wrap = document.createElement('div');
    wrap.className = 'account-menu-wrap';
    anchor.parentNode.insertBefore(wrap, anchor);
    wrap.appendChild(anchor);
    anchor.setAttribute('aria-haspopup', 'menu');
    anchor.setAttribute('aria-expanded', 'false');
    anchor.href = '#account-menu';
    anchor.title = 'Mở menu tài khoản';
    if (user) paintAvatar(anchor, user); else anchor.textContent = '👤';
    const menu = document.createElement('div');
    menu.className = 'account-popover'; menu.setAttribute('role', 'menu'); menu.hidden = true;
    if (user) {
      const title = document.createElement('div'); title.className = 'account-popover-head';
      const name = document.createElement('strong'); name.textContent = user.fullname || user.username;
      const sub = document.createElement('small'); sub.textContent = user.role === 'admin' ? 'Quản trị viên' : user.username;
      title.append(name, sub); menu.appendChild(title);
      const entries = [
        ['fa-solid fa-user-gear', 'Quản lý tài khoản', '/account.html'],
        ['fa-solid fa-arrow-right-arrow-left', 'Đổi tài khoản', '/login.html?switch=1'],
        ['fa-solid fa-user-plus', 'Đăng ký tài khoản mới', '/signup.html'],
      ];
      if (user.role !== 'admin') entries.unshift(['fa-solid fa-calendar-check','Lịch đặt của tôi','/booking-history.html']);
      else entries.unshift(['fa-solid fa-gauge','Quản trị hệ thống','/admin/dashboard.html']);
      entries.forEach(([icon, text, href]) => {
        const a = document.createElement('a'); a.href=href;
        if (text==='Đổi tài khoản' || text==='Đăng ký tài khoản mới') a.addEventListener('click',async event=>{ event.preventDefault(); try{await fetch('/api/auth/logout',{method:'POST'});}catch(_){} sessionStorage.removeItem('au_user');location.href=href; });
        a.innerHTML=`<i class="${icon}"></i> `; a.append(document.createTextNode(text));menu.appendChild(a);
      });
    } else {
      const title=document.createElement('div'); title.className='account-popover-head';title.innerHTML='<strong>Chào mừng bạn</strong><small>Đăng nhập để đặt và quản lý lịch sân</small>';menu.appendChild(title);
      [['Đăng nhập','/login.html'],['Đăng ký tài khoản','/signup.html']].forEach(([label,url])=>{
        const a=document.createElement('a');a.href=url;a.textContent=label;menu.appendChild(a);
      });
    }
    const toggle=document.createElement('button');toggle.type='button';toggle.dataset.themeToggle='';toggle.setAttribute('aria-label','Đổi chế độ giao diện');toggle.textContent='';
    toggle.addEventListener('click',()=>setTheme(root.dataset.theme === 'dark' ? 'light':'dark'));
    menu.appendChild(toggle);
    if (user) {
      const out=document.createElement('button');out.type='button';out.className='account-danger';out.innerHTML='<i class="fa-solid fa-right-from-bracket"></i> Đăng xuất';
      out.addEventListener('click',logout);menu.appendChild(out);
    }
    wrap.appendChild(menu);
    anchor.addEventListener('click',event=>{
      event.preventDefault();event.stopPropagation();
      const open = menu.hidden; menu.hidden = !open; wrap.classList.toggle('open',open); anchor.setAttribute('aria-expanded',String(open));
    });
    document.addEventListener('click',event=>{if(!wrap.contains(event.target)){menu.hidden=true;wrap.classList.remove('open');anchor.setAttribute('aria-expanded','false');}});
    document.addEventListener('keydown',event=>{if(event.key==='Escape'){menu.hidden=true;wrap.classList.remove('open');anchor.setAttribute('aria-expanded','false');anchor.focus();}});
    setTheme(root.dataset.theme);
  }
  async function loadIdentity() {
    try {
      const res=await fetch('/api/auth/me',{cache:'no-store'});
      const data=await res.json();
      if(res.ok && data.success) return data.user;
    } catch (_) {}
    return null;
  }
  async function populateBusiness() {
    try {
      const response = await fetch('/api/settings/public');
      const result = await response.json();
      if (!response.ok || !result.success) return;
      document.querySelectorAll('[data-business-address]').forEach(e => e.textContent = result.data.business_address);
      document.querySelectorAll('[data-business-phone]').forEach(e => e.textContent = result.data.support_phone || 'Liên hệ tại quầy');
      document.querySelectorAll('[data-business-hours]').forEach(e => e.textContent = `${result.data.open_time}–${result.data.close_time}`);
    } catch (_) {}
  }
  async function verifyVersion() {
    try {
      const res = await fetch('/api/version', { cache: 'no-store' });
      const data = await res.json();
      if (res.ok && data.version === '2.8.0') return;
    } catch (_) {}
    if (document.getElementById('au-version-warning')) return;
    const alertBox = document.createElement('div');
    alertBox.id = 'au-version-warning';
    alertBox.setAttribute('role', 'alert');
    alertBox.style.cssText = 'padding:12px 20px;background:#fff4dd;color:#864300;border-bottom:1px solid #f5d49f;text-align:center;font-size:13px;font-weight:600;position:relative;z-index:9999';
    alertBox.textContent = 'Không kết nối được với AU Badminton V2.8. Hãy kiểm tra máy chủ cổng 8080.';
    document.body.prepend(alertBox);
  }
  function decorateAdminTopbar(user) {
    const top = document.querySelector('.admin-topbar');
    if (!top || top.querySelector('.admin-topbar-meta')) return;
    const meta = document.createElement('div');
    meta.className = 'admin-topbar-meta';
    const online = document.createElement('span');
    online.className = 'admin-topbar-pill only-wide';
    online.innerHTML = '<span class="live-dot"></span>Hệ thống đang hoạt động';
    const date = document.createElement('span');
    date.className = 'admin-topbar-pill only-wide';
    date.textContent = new Intl.DateTimeFormat('vi-VN',{weekday:'long',day:'2-digit',month:'2-digit',year:'numeric'}).format(new Date());
    const userChip = document.createElement('a');
    userChip.className = 'admin-topbar-user';
    userChip.href = '../account.html';
    const avatar = document.createElement('span');avatar.className='au-avatar';
    paintAvatar(avatar,user);
    const label = document.createElement('span'); label.textContent=user.fullname || 'Quản trị viên';
    userChip.append(avatar,label);
    meta.append(online,date,userChip);
    const toggle=document.createElement('button'); toggle.type='button';toggle.className='theme-topbar';
    toggle.dataset.themeToggle='';toggle.setAttribute('aria-label','Đổi giao diện sáng tối');
    toggle.addEventListener('click',()=>setTheme(root.dataset.theme === 'dark' ? 'light':'dark'));
    meta.appendChild(toggle);
    top.appendChild(meta);
    const oldAvatar=top.querySelector(':scope > div > .au-avatar');
    if(oldAvatar && oldAvatar.closest('.admin-topbar-meta')===null) oldAvatar.parentElement.remove();
    setTheme(root.dataset.theme);
    document.querySelectorAll('.sidebar-user strong').forEach(e=>e.textContent=user.fullname || 'Quản trị viên');
  }

  function decorateAdminPage() {
    const main = document.querySelector('.admin-main');
    if (!main || main.querySelector('.admin-hero')) return;
    const key = location.pathname.split('/').pop();
    const pages = {
      'dashboard.html':['Tổng quan hôm nay','Theo dõi sân, lịch đặt và các hoạt động cần ưu tiên','<a class="solid" href="bookings.html"><i class="fa-solid fa-calendar-check"></i> Quản lý lịch đặt</a><a href="report.html"><i class="fa-solid fa-chart-line"></i> Xem báo cáo</a>'],
      'bookings.html':['Điều hành lịch đặt','Duyệt lịch, theo dõi trạng thái và hỗ trợ khách hàng','<a class="solid" href="courts.html">Danh sách sân</a><a href="report.html">Thống kê</a>'],
      'courts.html':['Danh mục sân cầu lông','Cập nhật thông tin sân và theo dõi lượt sử dụng','<a class="solid" href="bookings.html">Lịch đặt sân</a><a href="iot.html">Trạng thái đèn</a>'],
      'customers.html':['Quan hệ khách hàng','Thống kê số lượt đặt và tỷ lệ khách quay lại','<a class="solid" href="bookings.html">Quản lý đặt sân</a><a href="report.html">Phân tích giá trị đơn</a>'],
      'iot.html':['Điều khiển trạng thái đèn','Quản lý chế độ bật, tắt và mô phỏng tự động theo lịch','<a class="solid" href="courts.html">Quản lý sân</a><a href="settings.html">Cấu hình</a>'],
      'report.html':['Báo cáo kinh doanh','So sánh giá trị đơn xác nhận theo tháng và theo năm','<a class="solid" href="bookings.html">Kiểm tra đơn đặt sân</a><button type="button" id="printReportQuick">In báo cáo</button>'],
      'settings.html':['Thiết lập vận hành','Quản lý hồ sơ cơ sở, giờ hoạt động và tùy chọn sử dụng','<a class="solid" href="../account.html">Tài khoản quản trị</a><a href="report.html">Báo cáo kinh doanh</a>']
    };
    const config = pages[key];
    if (!config) return;
    const hero = document.createElement('section');hero.className='admin-hero';
    hero.innerHTML = `<div><small>AU BADMINTON · OPERATIONS</small><h3>${config[0]}</h3><p>${config[1]}</p></div><div class="admin-hero-actions">${config[2]}</div>`;
    main.prepend(hero);
    hero.querySelector('#printReportQuick')?.addEventListener('click',()=>window.print());
    if (key === 'dashboard.html') {
      const grid = document.createElement('nav'); grid.className='admin-quick-grid';grid.setAttribute('aria-label','Thao tác nhanh');
      grid.innerHTML = `
        <a class="admin-quick-item" href="bookings.html"><span class="admin-quick-icon"><i class="fa-solid fa-calendar-check"></i></span><span><strong>Xử lý đơn đặt</strong><small>Xem đơn mới và trạng thái</small></span><i class="fa-solid fa-angle-right"></i></a>
        <a class="admin-quick-item" href="courts.html"><span class="admin-quick-icon"><i class="fa-solid fa-table-tennis-paddle-ball"></i></span><span><strong>Quản lý sân</strong><small>Cập nhật sân và giá thuê</small></span><i class="fa-solid fa-angle-right"></i></a>
        <a class="admin-quick-item" href="report.html"><span class="admin-quick-icon"><i class="fa-solid fa-chart-line"></i></span><span><strong>Báo cáo vận hành</strong><small>Thống kê theo tháng / năm</small></span><i class="fa-solid fa-angle-right"></i></a>`;
      hero.after(grid);
    }
  }

  function setupMobileSidebar() {
    const sidebar = document.getElementById('adminSidebar');
    const top = document.querySelector('.admin-topbar');
    if (!sidebar || !top) return;
    const mobileButton = document.createElement('button');
    mobileButton.type = 'button';
    mobileButton.className = 'admin-mobile-toggle';
    mobileButton.setAttribute('aria-label', 'Mở điều hướng');
    mobileButton.innerHTML = '<i class="fa-solid fa-bars"></i>';
    top.prepend(mobileButton);
    const overlay = document.createElement('div');
    overlay.className = 'admin-mobile-backdrop';
    document.body.appendChild(overlay);
    function close() { sidebar.classList.remove('open');overlay.classList.remove('show'); }
    mobileButton.addEventListener('click', () => {
      const expanded = sidebar.classList.toggle('open');
      overlay.classList.toggle('show', expanded);
    });
    overlay.addEventListener('click', close);
    sidebar.querySelectorAll('a').forEach(a => a.addEventListener('click', close));
    document.addEventListener('keydown', e => { if (e.key === 'Escape') close(); });
  }
  async function initialize() {
    installCriticalMenuStyles();
    verifyVersion();
    const isAdminPage = location.pathname.includes('/admin/');
    if (!isAdminPage) populateBusiness();
    if (isAdminPage) {
      const user=await loadIdentity();
      if(!user || user.role!=='admin'){
        location.replace('/login.html?next='+encodeURIComponent(location.pathname));return;
      }
      document.body.classList.remove('admin-locked');
      setupMobileSidebar();
      decorateAdminTopbar(user);
      decorateAdminPage();
      const top=document.querySelector('.admin-topbar');
      if(top && !top.querySelector('[data-theme-toggle]')){
        const button=document.createElement('button');button.type='button';button.className='theme-topbar';button.dataset.themeToggle='';
        button.addEventListener('click',()=>setTheme(root.dataset.theme === 'dark' ? 'light':'dark'));
        top.appendChild(button);
      }
      setTheme(root.dataset.theme);
      return;
    }
    const anchor=document.querySelector('.au-navbar .au-avatar');
    if (!anchor) return;
    const user=await loadIdentity();
    if (user) sessionStorage.setItem('au_user', JSON.stringify(user));
    else sessionStorage.removeItem('au_user');
    accountMenu(anchor,user);
  }
  if(document.readyState==='loading') document.addEventListener('DOMContentLoaded',initialize);
  else initialize();
  window.AU = {logout,paintAvatar,loadIdentity,setTheme};
})();
