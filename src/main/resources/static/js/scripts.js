const swiper = new Swiper(".slide-content", {
    slidesPerView: 6,
    spaceBetween: 30,
    centerSlide: 'true',
    fade: 'true',
    grabCursor: 'true',
    pagination: {
        el: ".swiper-pagination",
        clickable: true,
        dynamicBullets: true
    },
    navigation: {
        nextEl: ".swiper-button-next",
        prevEl: ".swiper-button-prev",
    },

    breakpoints: {
        0: {
            slidesPerView: 1,
        },
        520: {
            slidesPerView: 2,
        },
        950: {
            slidesPerView: 4,
        },
        1900: {
            slidesPerView: 6,
        },
    },
});

(() => {let circle = document.querySelector(".user");

    circle.addEventListener("click", (e)=>{
        let target = e.target;
        if(target.classList.contains("circle")){
            circle.querySelector(".active").classList.remove("active");
            target.classList.add("active");
            document.querySelector(".main-images .active").classList.remove("active");
            document.querySelector(`.main-images .${target.id}`).classList.add("active");
        }
    })
})
();