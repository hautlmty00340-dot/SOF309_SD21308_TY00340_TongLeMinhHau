(() => {
  const products = [
    {
      id: 'headphones',
      name: 'Tai nghe Bluetooth chống ồn PulseTune Pro',
      category: 'Công nghệ',
      price: 780000,
      oldPrice: 1500000,
      rating: '4.9',
      reviews: '2.1k',
      badge: 'Hot deal',
      image: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=85'
    },
    {
      id: 'watch',
      name: 'Đồng hồ thông minh AMOLED thế hệ mới',
      category: 'Công nghệ',
      price: 970000,
      oldPrice: 1290000,
      rating: '4.8',
      reviews: '860',
      badge: 'Bán chạy',
      image: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=800&q=85'
    },
    {
      id: 'sneakers',
      name: 'Giày thể thao năng động Urban',
      category: 'Thời trang',
      price: 590000,
      oldPrice: 890000,
      rating: '4.9',
      reviews: '1.4k',
      badge: 'Xu hướng',
      image: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=85'
    },
    {
      id: 'camera',
      name: 'Máy ảnh film Mini Moment',
      category: 'Công nghệ',
      price: 2190000,
      oldPrice: 2590000,
      rating: '4.7',
      reviews: '94',
      badge: 'Mới',
      image: 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=800&q=85'
    },
    {
      id: 'chair',
      name: 'Ghế công thái học PulseFit cao cấp',
      category: 'Gia dụng',
      price: 2150000,
      oldPrice: 3200000,
      rating: '4.8',
      reviews: '890',
      badge: 'Hot deal',
      image: 'https://images.unsplash.com/photo-1503602642458-232111445657?auto=format&fit=crop&w=800&q=85'
    },
    {
      id: 'skincare',
      name: 'Bộ chăm sóc da Glow Daily',
      category: 'Làm đẹp',
      price: 590000,
      oldPrice: 750000,
      rating: '4.9',
      reviews: '412',
      badge: 'Yêu thích',
      image: 'https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b?auto=format&fit=crop&w=800&q=85'
    },
    {
      id: 'tablet',
      name: 'Máy tính bảng màn hình Retina 10.9 inch',
      category: 'Công nghệ',
      price: 9490000,
      oldPrice: 10990000,
      rating: '4.9',
      reviews: '2.1k',
      badge: 'Mall',
      image: 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&w=800&q=85'
    },
    {
      id: 'backpack',
      name: 'Ba lô laptop chống nước Urban Explorer',
      category: 'Thời trang',
      price: 460000,
      oldPrice: 680000,
      rating: '4.8',
      reviews: '3.4k',
      badge: 'Mall',
      image: 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=800&q=85'
    }
  ];

  const filters = ['Tất cả', 'Thời trang', 'Công nghệ', 'Gia dụng', 'Làm đẹp'];
  let cartCount = 0;
  let activeCategory = 'Tất cả';

  function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN').format(price) + '₫';
  }

  function renderChrome() {
    const header = document.querySelector('header');
    const footer = document.querySelector('footer');
    if (!header || !footer) return;

    header.className = 'store-header';
    header.innerHTML = `
      <div class="store-topbar">
        <div class="store-shell store-topbar-inner">
          <div class="store-topbar-left">
            <span><span class="material-symbols-outlined" aria-hidden="true">local_shipping</span> Giao hàng miễn phí toàn quốc đơn từ 299k</span>
            <a href="tel:19006868"><span class="material-symbols-outlined" aria-hidden="true">support_agent</span> Hotline: <strong>1900 6868</strong></a>
          </div>
          <div class="store-topbar-right">
            <a href="#register">Tải ứng dụng ShopPulse</a>
            <span class="store-language">◎ &nbsp; VIE <span>|</span> ENG</span>
          </div>
        </div>
      </div>
      <div class="store-mainbar store-shell">
        <a class="store-brand" href="#home" aria-label="ShopPulse - Trang chủ">
          <span class="store-brand-mark">S</span>
          <span>Shop<span>Pulse</span></span>
        </a>
        <form class="store-search" id="storeSearchForm" role="search">
          <button type="button" class="store-search-category">Tất cả danh mục <span class="material-symbols-outlined" aria-hidden="true">expand_more</span></button>
          <span class="store-search-divider"></span>
          <input id="storeSearchInput" type="search" placeholder="Săn deal công nghệ, thời trang và bách hóa..." aria-label="Tìm sản phẩm"/>
          <button class="store-search-submit" type="submit"><span class="material-symbols-outlined" aria-hidden="true">search</span><span>Tìm</span></button>
        </form>
        <div class="store-actions">
          <button class="store-icon-button" type="button" aria-label="Yêu thích"><span class="material-symbols-outlined" aria-hidden="true">favorite</span></button>
          <button class="store-icon-button store-header-cart" id="storeHeaderCart" type="button" aria-label="Giỏ hàng">
            <span class="material-symbols-outlined" aria-hidden="true">shopping_bag</span><span class="store-action-count" id="storeHeaderCartCount">0</span>
          </button>
          <a class="store-account" href="#login"><span class="material-symbols-outlined" aria-hidden="true">account_circle</span><span><small>Tài khoản</small><strong>Đăng nhập</strong></span></a>
        </div>
      </div>
      <nav class="store-nav">
        <div class="store-shell store-nav-inner">
          <a class="is-current" href="#home">Trang chủ</a>
          <a href="#flash-sale-section">Deal sốc hôm nay</a>
          <a href="#mall-section">Hàng mới về</a>
          <a href="#categories-section">Điện thoại &amp; Tablet</a>
          <a href="#categories-section">Thời trang nam/nữ</a>
          <a href="#categories-section">Đồ gia dụng thông minh</a>
          <a href="#categories-section">Làm đẹp &amp; Sức khỏe</a>
          <a class="store-nav-sale" href="#flash-sale-section">Flash Sale giảm tới 50%</a>
        </div>
      </nav>`;

    footer.className = 'store-footer';
    footer.innerHTML = `
      <section class="store-newsletter">
        <div class="store-shell store-newsletter-inner">
          <div><h2><span class="material-symbols-outlined" aria-hidden="true">mark_email_read</span> Đăng ký nhận tin khuyến mãi từ ShopPulse</h2><p>Nhận ngay voucher ưu đãi 100.000₫ cho đơn hàng đầu tiên cùng thông báo sale độc quyền.</p></div>
          <form id="newsletterForm"><label class="sr-only" for="newsletterEmail">Địa chỉ email</label><input id="newsletterEmail" type="email" placeholder="Nhập địa chỉ email của bạn..." required/><button type="submit">Đăng ký</button></form>
        </div>
      </section>
      <div class="store-shell store-footer-main">
        <section class="store-footer-about">
          <a class="store-brand" href="#home"><span class="store-brand-mark">S</span><span>Shop<span>Pulse</span></span></a>
          <p>ShopPulse - Nền tảng thương mại điện tử trải nghiệm mua sắm thông minh, bảo mật, tiện lợi với hàng triệu sản phẩm chính hãng hàng đầu tại Việt Nam.</p>
          <span><span class="material-symbols-outlined" aria-hidden="true">location_on</span> Tầng 12, Tòa nhà Innovation, Quận 1, TP. Hồ Chí Minh</span>
          <a href="tel:19006868"><span class="material-symbols-outlined" aria-hidden="true">call</span> 1900 6868 (8:00 - 21:00 hàng ngày)</a>
          <a href="mailto:cskh@shoppulse.vn"><span class="material-symbols-outlined" aria-hidden="true">mail</span> cskh@shoppulse.vn</a>
        </section>
        <section><h3>Chăm sóc khách hàng</h3><a href="#home">Trung tâm trợ giúp 24/7</a><a href="#home">Hướng dẫn đặt hàng &amp; thanh toán</a><a href="#home">Chính sách đổi trả</a><a href="#home">Phương thức vận chuyển</a><a href="#home">Chính sách hoàn tiền</a></section>
        <section><h3>Về ShopPulse</h3><a href="#home">Giới thiệu ShopPulse Việt Nam</a><a href="#home">Cơ hội nghề nghiệp &amp; Tuyển dụng</a><a href="#home">Đối tác</a><a href="#home">Điều khoản &amp; Chính sách sử dụng</a><a href="#home">Chính sách bảo mật dữ liệu</a></section>
        <section class="store-footer-cert"><h3>Chứng nhận &amp; bảo mật</h3><div>◉ &nbsp; ĐÃ ĐĂNG KÝ<br/><small>Bộ Công Thương Việt Nam</small></div><div>▣ &nbsp; SSL SECURED<br/><small>Mã hóa 256-bit an toàn</small></div></section>
      </div>
      <div class="store-payment"><div class="store-shell"><span>ĐỐI TÁC THANH TOÁN &nbsp; Visa &nbsp; Mastercard &nbsp; MoMo &nbsp; VNPay &nbsp; COD</span><span>ĐỐI TÁC VẬN CHUYỂN &nbsp; GHN &nbsp; GHTK &nbsp; Viettel Post &nbsp; GrabExpress</span></div></div>
      <div class="store-copyright"><div class="store-shell"><span>© 2025 ShopPulse Technology JSC. Tôn trọng bản quyền.</span><span>Quy chế hoạt động &nbsp; · &nbsp; Giải quyết tranh chấp &nbsp; · &nbsp; Chính sách cookie</span></div></div>`;

    document.getElementById('storeSearchForm').addEventListener('submit', (event) => {
      event.preventDefault();
      const input = document.getElementById('storeSearchInput');
      if (window.location.hash !== '#home') window.location.hash = 'home';
      window.setTimeout(() => {
        const search = document.getElementById('storeSearchInput');
        if (search) {
          search.dispatchEvent(new Event('input'));
          document.getElementById('recommendations-section').scrollIntoView({ behavior: 'smooth' });
        }
      }, 50);
    });

    document.getElementById('storeHeaderCart').addEventListener('click', () => showToast(cartCount ? `Giỏ hàng của bạn có ${cartCount} sản phẩm.` : 'Giỏ hàng của bạn đang trống.'));
    document.getElementById('newsletterForm').addEventListener('submit', (event) => {
      event.preventDefault();
      showToast('Cảm ơn bạn đã đăng ký nhận tin ShopPulse!');
      event.currentTarget.reset();
    });
  }

  function productCard(product, index) {
    return `
      <article class="home-product-card" data-category="${product.category}" data-search="${`${product.name} ${product.category}`.toLocaleLowerCase('vi')}">
        <div class="home-product-image-wrap">
          <span class="home-product-badge">${product.badge}</span>
          <button class="home-favorite" type="button" aria-label="Thêm ${product.name} vào yêu thích" aria-pressed="false"><span class="material-symbols-outlined" aria-hidden="true">favorite</span></button>
          <img class="home-product-image" src="${product.image}" alt="${product.name}" loading="lazy"/>
        </div>
        <div class="home-product-info">
          <h3>${product.name}</h3>
          <div class="home-product-rating"><span aria-hidden="true">★</span> ${product.rating} <span>· Đã bán ${product.reviews}</span></div>
          <div class="home-product-price"><strong>${formatPrice(product.price)}</strong><del>${formatPrice(product.oldPrice)}</del></div>
          <div class="store-product-tags"><span>${index % 2 ? 'Trả góp 0%' : 'Freeship'}</span><span>Ưu đãi Pulse+</span></div>
          <button class="home-add-to-cart" type="button" data-product-id="${product.id}"><span class="material-symbols-outlined" aria-hidden="true">add_shopping_cart</span> Thêm vào giỏ</button>
        </div>
      </article>`;
  }

  function getMarkup() {
    const categories = [
      ['smartphone', 'Điện thoại & Phụ kiện', '8.4k+ deal'],
      ['laptop_mac', 'Laptop & IT', '3.2k+ deal'],
      ['checkroom', 'Thời trang nam nữ', '15.9k+ deal'],
      ['blender', 'Đồ gia dụng', '5.1k+ deal'],
      ['spa', 'Mỹ phẩm & sức khỏe', '9.7k+ deal'],
      ['child_care', 'Mẹ & em bé', '4.3k+ deal'],
      ['fitness_center', 'Thể thao & dã ngoại', '2.8k+ deal'],
      ['shopping_basket', 'Bách hóa online', '12.1k+ deal']
    ];
    const shortcuts = [
      ['confirmation_number', 'Mã giảm giá'], ['bolt', 'Deal 1K sốc'],
      ['workspace_premium', 'PulseClub VIP'], ['savings', 'Săn xu đổi quà'],
      ['credit_card', 'Trả sau 0% lãi'], ['qr_code_scanner', 'Nạp thẻ & Bill']
    ];
    const brands = [
      ['Apple', 'Đến 15%'], ['Samsung', 'Đến 20%'], ['Sony Audio', 'Tặng quà'], ['L’Oréal Paris', 'Mua 1 tặng 1'], ['Anker Innovations', 'Freeship'], ['Nike Flagship', 'Ưu đãi 30%']
    ];

    return `
      <div class="home-page store-home">
        <div class="store-shell">
          <section class="store-hero-layout" aria-label="Ưu đãi nổi bật">
            <div class="store-hero">
              <div class="store-hero-top"><span>✦ &nbsp; SIÊU HỘI SHOPPULSE MEGA SALE</span><span>Mã độc quyền: <strong>MEGA50K</strong></span></div>
              <div class="store-hero-copy">
                <h1>Giảm Đến 50%<br/><span>Freeship 0Đ</span> Toàn Sàn</h1>
                <p>Hàng hiệu giảm sâu từ công nghệ cao cấp, thời trang dẫn đầu đến đời sống thông minh. Ưu đãi độc quyền chỉ có trong tuần lễ vàng.</p>
                <div><a href="#recommendations-section" class="store-button-orange">Khám phá ngay <span class="material-symbols-outlined" aria-hidden="true">arrow_forward</span></a><a href="#flash-sale-section" class="store-button-light">Săn deal chớp nhoáng</a></div>
              </div>
              <div class="store-hero-bottom"><span>● ━━━ &nbsp; ○ &nbsp; ○</span><span>Cập nhật mỗi 30 phút</span></div>
            </div>
            <div class="store-promo-tiles">
              <a class="store-promo-tile store-promo-tech" href="#flash-sale-section"><span><small>TECH FEST 2025</small><strong>Công Nghệ Đỉnh Cao</strong><em>Giảm ngay 30% cho thiết bị âm thanh &amp; phụ kiện chính hãng.</em></span><b>Săn ngay ›</b></a>
              <a class="store-promo-tile store-promo-fashion" href="#categories-section"><span><small>SUMMER ESSENTIALS</small><strong>Thời Trang Năng Động</strong><em>Đồng giá chỉ từ 99.000₫, phong cách trẻ trung đường phố.</em></span><b>Xem bộ sưu tập ›</b></a>
            </div>
          </section>

          <section class="store-trust-row" aria-label="Cam kết ShopPulse">
            <article><span class="material-symbols-outlined">verified_user</span><div><strong>100% Chính hãng</strong><small>Cam kết bồi hoàn 200% nếu phát hiện giả</small></div></article>
            <article><span class="material-symbols-outlined">local_shipping</span><div><strong>Freeship toàn quốc</strong><small>Đơn hàng từ 299.000₫ giao tốc hành</small></div></article>
            <article><span class="material-symbols-outlined">sync_alt</span><div><strong>Đổi trả 30 ngày</strong><small>Thủ tục siêu tốc, miễn phí lấy hàng</small></div></article>
            <article><span class="material-symbols-outlined">headset_mic</span><div><strong>Tư vấn 24/7</strong><small>Đội ngũ chuyên viên tận tình</small></div></article>
          </section>

          <section class="store-shortcuts" aria-label="Lối tắt mua sắm">
            ${shortcuts.map(([icon, label]) => `<a href="#flash-sale-section"><span class="material-symbols-outlined">${icon}</span><strong>${label}</strong></a>`).join('')}
          </section>
        </div>

        <section class="store-sale-section" id="flash-sale-section">
          <div class="store-shell">
            <div class="store-section-heading store-sale-heading">
              <div><span class="store-heading-icon">⚡</span><div><h2>Flash Sale Hôm Nay</h2><p>Kết thúc sau: <b id="saleHours">02</b> : <b id="saleMinutes">45</b> : <b id="saleSeconds">18</b></p></div></div>
              <a href="#recommendations-section">Xem tất cả deal sốc <span class="material-symbols-outlined">arrow_forward</span></a>
            </div>
            <div class="store-sale-grid">
              ${products.slice(0, 5).map((product, index) => `<article class="store-sale-card"><div class="store-sale-image"><span>-${[48, 32, 25, 18, 33][index]}%</span><img src="${product.image}" alt="${product.name}" loading="lazy"/></div><h3>${product.name}</h3><div class="store-sale-prices"><strong>${formatPrice(product.price)}</strong><del>${formatPrice(product.oldPrice)}</del></div><div class="store-progress"><i style="width:${[82, 67, 74, 41, 53][index]}%"></i></div><div class="store-sold"><span>Đã bán ${[210, 156, 98, 76, 110][index]}</span><b>${index % 2 ? 'Sắp hết' : 'Đang nóng'}</b></div></article>`).join('')}
            </div>
          </div>
        </section>

        <div class="store-shell">
          <section class="store-categories-section" id="categories-section">
            <div class="store-section-heading"><div><h2>Danh Mục Nổi Bật</h2><p>Lựa chọn hàng nghìn sản phẩm theo nhu cầu mua sắm của bạn</p></div><div class="store-round-arrows"><button type="button" aria-label="Danh mục trước">‹</button><button type="button" aria-label="Danh mục tiếp">›</button></div></div>
            <div class="store-category-grid">${categories.map(([icon, label, count], index) => `<a href="#recommendations-section" class="store-category-card"><span class="store-category-icon tone-${index % 4}"><span class="material-symbols-outlined">${icon}</span></span><strong>${label}</strong><small>${count}</small></a>`).join('')}</div>
          </section>
        </div>

        <section class="store-mall-section" id="mall-section"><div class="store-shell">
          <div class="store-section-heading store-mall-heading"><div><span class="store-mall-label">PULSE MALL &nbsp; <small>100% AUTHENTIC BRANDS</small></span><h2>Thương Hiệu Chính Hãng Hàng Đầu</h2><p>Trải nghiệm mua sắm bảo hành chính hãng cùng ưu đãi trả góp 0%</p></div><div class="store-brand-tabs"><button class="is-active" type="button">Bán chạy nhất</button><button type="button">Hàng mới cập bến</button><button type="button">Đề xuất xu hướng</button></div></div>
          <div class="store-brand-grid">${brands.map(([name, deal], index) => `<article><span class="store-brand-logo brand-${index}">${name}</span><strong>${name} Official</strong><small>${deal}</small><a href="#recommendations-section">Vào gian hàng</a></article>`).join('')}</div>
        </div></section>

        <section class="store-recommendations store-shell" id="recommendations-section">
          <div class="store-section-heading store-recommend-heading"><div><h2><span>✦</span> Gợi Ý Dành Riêng Cho Bạn</h2><p>Sản phẩm dựa trên thói quen tìm kiếm &amp; sở thích mua sắm</p></div><div class="home-categories" aria-label="Lọc sản phẩm">${filters.map((filter, index) => `<button class="home-category${index === 0 ? ' is-active' : ''}" type="button" data-category-filter="${filter}" aria-pressed="${index === 0}">${filter}</button>`).join('')}</div></div>
          <div class="home-product-grid" id="homeProductGrid">${products.map(productCard).join('')}</div>
          <p class="home-empty-results" id="homeEmptyResults" hidden>Chưa tìm thấy sản phẩm phù hợp. Hãy thử từ khóa khác nhé.</p>
          <div class="store-load-more"><button type="button">Xem thêm 100+ sản phẩm nổi bật khác <span class="material-symbols-outlined">expand_more</span></button></div>
        </section>

        <section class="store-member-section"><div class="store-shell"><div class="home-member-banner">
          <div class="home-member-icon"><span class="material-symbols-outlined">loyalty</span></div>
          <div><span>Dành riêng cho khách hàng mới</span><h2>Gia nhập ShopPulse nhận ngay gói quà 200.000₫</h2><p>Tặng voucher chào bạn mới cùng 10 lượt miễn phí vận chuyển 0Đ khi hoàn tất đăng ký.</p></div>
          <a href="#register">Đăng ký nhận quà ngay <span class="material-symbols-outlined">arrow_forward</span></a>
        </div></div></section>
        <div class="home-toast-region" id="homeToastRegion" aria-live="polite"></div>
        <aside class="store-floating-tools"><button type="button" id="storeChatButton" aria-label="Hỗ trợ trực tuyến"><span class="material-symbols-outlined">chat</span></button><button type="button" id="storeTopButton" aria-label="Lên đầu trang"><span class="material-symbols-outlined">arrow_upward</span></button></aside>
      </div>`;
  }

  function initialize() {
    const grid = document.getElementById('homeProductGrid');
    const search = document.getElementById('storeSearchInput');
    const emptyResults = document.getElementById('homeEmptyResults');
    const categoryButtons = [...document.querySelectorAll('[data-category-filter]')];
    activeCategory = 'Tất cả';

    function filterProducts() {
      const query = search.value.trim().toLocaleLowerCase('vi');
      let visibleCount = 0;
      grid.querySelectorAll('.home-product-card').forEach((card) => {
        const category = card.dataset.category;
        const matchesCategory = activeCategory === 'Tất cả'
          || category === activeCategory
          || (activeCategory === 'Gia dụng' && category === 'Gia dụng');
        const isVisible = matchesCategory && card.dataset.search.includes(query);
        card.hidden = !isVisible;
        if (isVisible) visibleCount++;
      });
      emptyResults.hidden = visibleCount > 0;
    }

    categoryButtons.forEach((button) => {
      button.addEventListener('click', () => {
        activeCategory = button.dataset.categoryFilter;
        categoryButtons.forEach((item) => {
          const isActive = item === button;
          item.classList.toggle('is-active', isActive);
          item.setAttribute('aria-pressed', String(isActive));
        });
        filterProducts();
      });
    });

    search.addEventListener('input', filterProducts);
    grid.addEventListener('click', (event) => {
      const favoriteButton = event.target.closest('.home-favorite');
      if (favoriteButton) {
        const isFavorite = favoriteButton.getAttribute('aria-pressed') !== 'true';
        favoriteButton.setAttribute('aria-pressed', String(isFavorite));
        favoriteButton.setAttribute('aria-label', isFavorite ? 'Đã thêm vào yêu thích' : 'Đã bỏ khỏi yêu thích');
        return;
      }

      const cartButton = event.target.closest('.home-add-to-cart');
      if (!cartButton) return;
      const product = products.find((item) => item.id === cartButton.dataset.productId);
      if (!product) return;
      cartCount++;
      document.getElementById('storeHeaderCartCount').textContent = String(cartCount);
      showToast(`${product.name} đã được thêm vào giỏ hàng.`);
    });

    document.getElementById('storeChatButton').addEventListener('click', () => showToast('Đội ngũ ShopPulse luôn sẵn sàng hỗ trợ bạn 24/7.'));
    document.getElementById('storeTopButton').addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));

    let remainingSeconds = 2 * 60 * 60 + 45 * 60 + 18;
    const countdownInterval = window.setInterval(() => {
      if (!document.getElementById('saleHours')) {
        window.clearInterval(countdownInterval);
        return;
      }
      remainingSeconds = remainingSeconds > 0 ? remainingSeconds - 1 : 3 * 60 * 60;
      document.getElementById('saleHours').textContent = String(Math.floor(remainingSeconds / 3600)).padStart(2, '0');
      document.getElementById('saleMinutes').textContent = String(Math.floor((remainingSeconds % 3600) / 60)).padStart(2, '0');
      document.getElementById('saleSeconds').textContent = String(remainingSeconds % 60).padStart(2, '0');
    }, 1000);
  }

  function showToast(message) {
    const region = document.getElementById('homeToastRegion');
    if (!region) return;
    const toast = document.createElement('div');
    toast.className = 'home-toast';
    const icon = document.createElement('span');
    icon.className = 'material-symbols-outlined';
    icon.setAttribute('aria-hidden', 'true');
    icon.textContent = 'check_circle';
    const text = document.createElement('span');
    text.textContent = message;
    toast.append(icon, text);
    region.appendChild(toast);
    window.setTimeout(() => toast.remove(), 3000);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', renderChrome, { once: true });
  } else {
    renderChrome();
  }
  window.getShopHomeMarkup = getMarkup;
  window.initializeShopHome = initialize;
})();
