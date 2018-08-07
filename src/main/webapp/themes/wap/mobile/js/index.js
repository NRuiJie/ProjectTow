/**
 * Created by Andste on 2016/12/5.
 */
$(function () {
    var module = new Module();
    var indexScroll;
    var searchNav = $('#index-search-nav');

    init();

    function init() {
        module.scrollToTopControl.init({
            rollListen: false,
            clickToTop: function () {
                indexScroll.isAnimating = false;
                setTimeout(function () {
                    indexScroll.scrollTo(0, 0, 300, IScroll.utils.ease.quadratic)
                }, 20)
            }
        });

        bindEvents();
    }

    function bindEvents() {

        //  初始化iscroll
        initIScroll();
        indexScroll.on('scroll', updateHeader);
        indexScroll.on('scrollEnd', updateHeader);

        //  初始化Swiper【广告横幅】
        initSwiper();

        //  商城公告事件绑定
        storeEvents();

        //  新品上市事件绑定
        newListingEvents();

        //  特别推荐
        sellingGoodsEvents();

        //  搜索栏事件绑定
        searchEvents();
    }

    function initIScroll() {
        indexScroll = new IScroll('#index', {
            probeType   : 3,
            mouseWheel  : false,
            disableTouch: false,
            tap         : true
        });
        document.addEventListener('touchmove', function (e) { e.preventDefault() }, false);
    }

    //  更新header透明度
    function updateHeader () {
        var _top = -(this.y>>0);
        _top > 150 ? module.scrollToTopControl.show() : module.scrollToTopControl.hide();
        _top < 0 ? searchNav.addClass('hide') : searchNav.removeClass('hide');
        if(_top < 0){_top = 0}
        if(_top > 80){_top = 80}
        $('#index-search-nav').css({backgroundColor: 'rgba(201, 21, 35, '+ _top/100 +')'})
    }

    //  初始化广告横幅
    function initSwiper() {
        var advSwiper = new Swiper('.swiper-container1', {
            loop: true,
            autoplay: 3000,
            autoplayDisableOnInteraction : false,
            pagination : '.swiper-pagination1',
            passiveListeners: false,
            onTap: function (swiper) {
                var $this = $(swiper['clickedSlide']), _href = $this.attr('data-href');
                _href && (location.href = _href);
            }
        });
        var advSwiper2 = new Swiper('.swiper-container2', {
            loop: true,
            autoplay: 3000,
            autoplayDisableOnInteraction : false,
            pagination : '.swiper-pagination2',
            passiveListeners: false,
            paginationType : 'fraction',
            onTap: function (swiper) {
                var $this = $(swiper['clickedSlide']), _href = $this.attr('data-href');
                _href && (location.href = _href);
            }
        })
    }

    //  商城公告事件绑定
    function storeEvents() {
        var bulletinBoard = $('.index-bulletin-board');
        var items         = bulletinBoard.find('.item'),
            itemsLen      = items.length;
        $('.more-bulletin-board').on('tap', function () {
            module.message.error('抱歉，没有更多了。。。');
            return false
        });
        if (itemsLen < 1) {
            return
        }
        var bulletinSwiper = new Swiper('.index-bulletin-board', {
            loop                        : true,
            autoplay                    : 3000,
            autoplayDisableOnInteraction: false,
            direction                   : 'vertical',
            preventLinksPropagation     : false
        })
    }

    //  新品上市事件绑定
    function newListingEvents() {
        var newListing = $('.list-new-listing'),
            items    = newListing.find('.item'),
            itemsLen = items.length;
        if(itemsLen < 4){return}
        newListing.css({width: itemsLen * 0.3 * 100 + '%'});
        //  初始化iscroll
        var newListingScroll = new IScroll("#content-new-listing", {
            scrollX: true,
            scrollY: false,
            probeType   : 1,
            mouseWheel  : false,
            disableTouch: false,
            tap         : true
        });
    }

    //  热销商品事件绑定
    function sellingGoodsEvents() {
        var sellingGoods = $('.content-selling-goods'),
            items    = sellingGoods.find('.item'),
            itemsLen = items.length;
        if(itemsLen < 1){return}
        var specialSwiper = new Swiper('.content-selling-goods', {
            loop: true,
            autoplay: 3000,
            autoplayDisableOnInteraction : false,
            effect : 'coverflow',
            slidesPerView: 3,
            centeredSlides: true,
            coverflow: {
                rotate      : 30,
                stretch     : 5,
                depth       : 100,
                modifier    : 2,
                slideShadows: true
            },
            passiveListeners: false,
            onTap: function (swiper) {
                var $this = $(swiper['clickedSlide']), _href = $this.attr('data-href');
                _href && (location.href = _href);
            }
        })
    }

    //  搜索栏事件绑定
    function searchEvents() {
        $('#search-nav').on('tap', function () {
            module.searchControl.init();
            return false
        })
    }

    //  整个文档加载完成后再次刷新iscroll
    window.onload = function () {
        indexScroll.refresh()
    };
});