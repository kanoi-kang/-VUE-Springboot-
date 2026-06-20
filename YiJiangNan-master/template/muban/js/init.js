$(function () {
  new WOW().init();
  var navType = true;
  if (/(iPhone|iPad|iPod|iOS|Android)/i.test(navigator.userAgent)) {
    navType = false;

  }
  if (navType) {
    $('.navdown').hover(function () {
      $(this).find('.dropdown-toggle').dropdown('toggle')
      $('.head').addClass('pc-nav-show');
    }, function () {
      $(this).removeClass('open');
      $(this).find('.dropdown-toggle').attr('aria-expanded', false);
    }
    );
    $('.head').hover(function () { }, function () {
      $(this).removeClass('pc-nav-show');
    });
    $('.pc-e-nav').hover(function () { }, function () {
      $('.head').removeClass('pc-nav-show');
    });
    $('.navdown .dropdown-toggle').on('click', function () {
      window.location.href = $(this).attr('href');
    });
  } else {
    $('html').addClass('cut')
    $(".dropdown-submenu").on("click", function (e) {

      $(this).find('.dropdown-menu').show();
      $(this).siblings().find('.dropdown-menu').hide();
      e.stopPropagation();
      e.preventDefault();
    });
    $('.dropdown-submenu .nav-link').click(function () {
      window.location.href = $(this).attr('href')
    })
  }
  $('.navbar-toggle').on('click', function () {
    if ($(this).is('.onk')) {
      $(this).removeClass('onk');
    } else {
      $(this).addClass('onk');
    }
  })
  var banner = new Swiper(".swp-banner", {
    spaceBetween: 0,
    // loop: true,
    autoplay: {
      delay: 5000,
      disableOnInteraction: false,
    },
    navigation: {
      nextEl: ".b-next",
      prevEl: ".b-prev",
    },
    pagination: {
      el: ".xubax",
      clickable: true,
    }
  });
  $(".select_box").click(function (event) {
    event.stopPropagation();
    $(this).find(".option").toggle();
    $(this).parent().siblings().find(".option").hide();
  });
  $(document).click(function (event) {
    var eo = $(event.target);
    if ($(".select_box").is(":visible") && eo.attr("class") != "option" && !eo.parent(".option").length)
      $('.option').hide();
  });
  $(".option li").click(function () {
    var check_value = $(this).text();
    var id = $(this).data('id');
    $('#select_field').val(id);
    $(this).parent().siblings(".select_txt").text(check_value);

  });
  $('.t-ico').on('click',function(){
    $("body,html").animate({
      scrollTop: 0
  }, 800);
  })
})
