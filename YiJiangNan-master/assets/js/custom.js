// ==================================================
// Project Name  :  Listright
// File          :  JS Base
// Version       :  1.0.0
// Last change   :  25 March 2024
// Author        :  Bitspeck
// ==================================================




(function ($) {
  "use strict";
  // grid-list - start
  // --------------------------------------------------
  $('.list').click(function (event) {
    event.preventDefault();
    $('.feature-item').removeClass('col-lg-6 col-md-6 col-md-4 grid-group-item 2-col 3-col');
    console.log("clicked");
    $('.feature-item').addClass('list-group-item col-md-12');
    $(".cat-img").css("height", $(".item-content").height());
  });

  $('.grid').click(function (event) {
    event.preventDefault();
    $('.feature-item').removeClass('list-group-item col-md-12 col-md-6');
    $('.2-col, .feature-item').addClass('grid-group-item col-md-6');
    $('.3-col, .feature-item').addClass('grid-group-item col-md-4');
  });
  // grid-list - end
  // --------------------------------------------------

  // custom-file Upload  Upload - start
  // --------------------------------------------------
  $(".custom-file-input").on("change", function () {
    var fileName = $(this).val().split("\\").pop();
    $(this).siblings(".custom-file-label").addClass("selected").html(fileName);
  });
  // custom-file Upload  Upload - end
  // --------------------------------------------------

  // left-sidebar - start
  // --------------------------------------------------
  $(document).ready(function () {
    $('#sidebar-dismiss, .overlay').on('click', function () {
      $('#left-sidebar').removeClass('active');
      $('.overlay').removeClass('active');
    });

    $('#left-sidebar-collapse').on('click', function () {
      $('#left-sidebar').addClass('active');

      $('.overlay').addClass('active');
      $('.collapse.in').toggleClass('in');
    });
  });
  // left-sidebar - end
  // --------------------------------------------------

  // back to top - start
  // --------------------------------------------------
  $(window).scroll(function () {
    if ($(this).scrollTop() > 200) {
      $('.backtotop:hidden').stop(true, true).fadeIn();
    } else {
      $('.backtotop').stop(true, true).fadeOut();
    }
  });
  $(function () {
    $(".scroll").on('click', function () {
      $("html,body").animate({
        scrollTop: $("#thetop").offset().top
      }, "slow");
      return false;
    });
  });
  // back to top - end
  // --------------------------------------------------





  // preloader - start
  // --------------------------------------------------
  $(window).on('load', function () {
    $('#preloader').fadeOut('slow', function () { $(this).remove(); });
  });
  // preloader - end
  // --------------------------------------------------


  // Counter - start
  // --------------------------------------------------
  $('.count-text').each(function () {
    $(this).prop('Counter', 0).animate({
      Counter: $(this).text()
    },
      {
        duration: 5000,
        easing: 'swing',
        step: function (now) {
          $(this).text(Math.ceil(now));
        }
      });
  });
  // Counter - end
  // --------------------------------------------------





  // popup image - start
  // --------------------------------------------------
  $('.popup-gallery').magnificPopup({
    delegate: 'a',
    type: 'image',
    tLoading: 'Loading image #%curr%...',
    mainClass: 'mfp-img-mobile',
    gallery: {
      enabled: true,
      navigateByImgClick: true,
      preload: [0, 1] // Will preload 0 - before current, and 1 after the current image
    },
    image: {
      tError: '<a href="%url%">The image #%curr%</a> could not be loaded.',
      titleSrc: function (item) {
        return item.el.attr('title') + '<small>by Marsel Van Oosten</small>';
      }
    }
  });
  // popup video - end
  // --------------------------------------------------


  // sticky menu - start
  // --------------------------------------------------
  var headerId = $(".sticky-header");

$(document).ready(function() {
    // Check initial scroll position on page load
    if ($(window).scrollTop() > 50) {
        headerId.removeClass("not-stuck").addClass("stuck");
    } else {
        headerId.removeClass("stuck").addClass("not-stuck");
    }

    // Add scroll event listener
    $(window).on('scroll', function () {
        var amountScrolled = $(window).scrollTop();
        if ($(this).scrollTop() > 50) {
            headerId.removeClass("not-stuck").addClass("stuck");
        } else {
            headerId.removeClass("stuck").addClass("not-stuck");
        }
    });
});

  // sticky menu - end
  // --------------------------------------------------


  // review-star - start
  // --------------------------------------------------
  $(document).ready(function () {
    function resetStarClass(e) {
      $(e).parent().removeClass('five-star');
      $(e).parent().removeClass('four-star');
      $(e).parent().removeClass('three-star');
      $(e).parent().removeClass('two-star');
      $(e).parent().removeClass('one-star');
      $(e).siblings(':first').removeAttr("checked");
      $(e).siblings(':nth(2)').removeAttr("checked");
      $(e).siblings(':nth(4)').removeAttr("checked");
      $(e).siblings(':nth(6)').removeAttr("checked");
      $(e).siblings(':last').removeAttr("checked");
    }
    $('.label-star5').on('click', function () {
      resetStarClass($(this));
      $(this).siblings(':first').attr("checked", "checked");
      $(this).parent().addClass('five-star');
    });
    $('.label-star4').on('click', function () {
      resetStarClass($(this));
      $(this).siblings(':nth(2)').attr("checked", "checked");
      $(this).parent().addClass('four-star');
    });
    $('.label-star3').on('click', function () {
      resetStarClass($(this));
      $(this).siblings(':nth(4)').attr("checked", "checked");
      $(this).parent().addClass('three-star');
    });
    $('.label-star2').on('click', function () {
      resetStarClass($(this));
      $(this).siblings(':nth(6)').attr("checked", "checked");
      $(this).parent().addClass('two-star');
    });
    $('.label-star1').on('click', function () {
      resetStarClass($(this));
      $(this).siblings(':last').attr("checked", "checked");
      $(this).parent().addClass('one-star');
    });
  });
  // review-star  - end
  // --------------------------------------------------


})(jQuery);