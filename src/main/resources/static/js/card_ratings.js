$('document').ready(()=> {
    const $widgets = $('.star-widget');

    $.each($widgets,(index, value) => {
        const $rating = $(value).attr('data-rating');
        const $stars = $(value).children('i');

        $.each($stars,(index,value) => {
            if($rating > index + 0.5) {
                $(value).addClass('yellow-color');
            }
        })
    })
})