let currentRating = 0;
const $rateForm = $('#rating')
const $addonId = $('#main-div').attr('data-addonId')
const $username = $rateForm.attr('data-username');

const getInitialInfo = () => {
    $.ajax({
        type: 'GET',
        url: window.location.origin + '/api/addons/' + $addonId,
    })
        .done(json => {
            let userRating = 0;
            let ratings = Object(json.rating);
            if (ratings.hasOwnProperty($username)) {
                userRating = Object.getOwnPropertyDescriptor(ratings, $username).value;
            }
            updateRatingView(userRating, json);
        })
}


getInitialInfo();

const vote = (rating) => {
    if (rating !== currentRating) {
        $.ajax({
            type: 'PUT',
            url: window.location.origin + '/api/addons/' + $addonId + '/rate/' + rating,
            contentType: 'application/json',
            dataType: 'json'
        }).done(json => {
            updateRatingView(rating, json);
        })
    } else {
        $.ajax({
            type: 'PUT',
            url: window.location.origin + '/api/addons/' + $addonId + '/removeRate',
            contentType: 'application/json',
            dataType: 'json'
        })
            .done(json => {
                updateRatingView(0, json);
            })
            .fail(() => {
                console.log('fail');
            })

    }


}




const updateRatingView = (userRating, addonJson) => {
    const $average = $('#rating-average');
    const $total = $('#ratings-total');
    const allStars = $('.star-rating');

    $average.text(addonJson.averageRating);
    $total.text(Object.keys(addonJson.rating).length);

    if (userRating === 0 && currentRating !== 0) {
        const $star = $('#rate-' + currentRating);
        $star.prop('checked', false);
        currentRating = userRating;


    } else if (userRating !== 0) {
        currentRating = userRating;
        let starId = 'rate-' + currentRating;
        const $star = $('#' + starId);
        $star.prop('checked', true);
    }
}

const delay = (time) => {
    return new Promise(resolve => setTimeout(resolve, time));
}

const updateDownloads = () => {
    delay(500).then(()=> {
        $.ajax({
            type: 'GET',
            url: window.location.origin + '/api/addons/' + $addonId + '/downloads',
            dataType: 'json'
        })
            .done(json => {
                $('#download-count').text(json);
            })
    })

}

$('#download-addon').on('click', () => updateDownloads());
