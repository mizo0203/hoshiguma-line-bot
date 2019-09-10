window.onload = function (e) {
    // init で初期化。基本情報を取得。
    // https://developers.line.biz/ja/reference/liff/#initialize-liff-app
    liff.init(
        data => {
            // Now you can call LIFF API
            // getProfile();
            // initializeApp(data);
            liff.getProfile()
                .then(profile => {
                    sampleAjax(data.context.groupId, data.context.userId, profile.displayName);
                })
                .catch((err) => {
                    window.alert("Error getProfile: " + err);
                    log("Error getProfile: " + err);
                });
        },
        err => {
            // LIFF initialization failed
            log("Error init: " + err);
            sampleAjax('Cdb9ae9a2dad3df6975645217c8058303', 'U0bf323d5458600230dbac8f10c59b3f2', 'Brown');
        }
    );

    // LIFF アプリを閉じる
    // https://developers.line.me/ja/reference/liff/#liffclosewindow()
    // document.getElementById('closewindowbutton').addEventListener('click', function () {
    //     liff.closeWindow();
    // });

    // ウィンドウを開く
    // https://developers.line.me/ja/reference/liff/#liffopenwindow()
    // document.getElementById('openwindowbutton').addEventListener('click', function () {
    //     liff.openWindow({
    //         url: 'https://line.me'
    //     });
    // });

    // document.getElementById('openwindowexternalbutton').addEventListener('click', function () {
    //     liff.openWindow({
    //         url: 'https://line.me',
    //         external: true
    //     });
    // });
};

// プロファイルの取得と表示
function getProfile() {
    // https://developers.line.me/ja/reference/liff/#liffgetprofile()
    liff.getProfile().then(function (profile) {
        document.getElementById('useridprofilefield').textContent = profile.userId;
        document.getElementById('displaynamefield').textContent = profile.displayName;
        log('profile.displayName: ' + profile.displayName);

        var profilePictureDiv = document.getElementById('profilepicturediv');
        if (profilePictureDiv.firstElementChild) {
            profilePictureDiv.removeChild(profilePictureDiv.firstElementChild);
        }
        var img = document.createElement('img');
        img.src = profile.pictureUrl;
        img.alt = "Profile Picture";
        img.width = 200;
        profilePictureDiv.appendChild(img);

        document.getElementById('statusmessagefield').textContent = profile.statusMessage;
    }).catch(function (error) {
        window.alert("Error getting profile: " + error);
    });
}

function initializeApp(data) {
    document.getElementById('languagefield').textContent = data.language;
    document.getElementById('viewtypefield').textContent = data.context.viewType;
    document.getElementById('useridfield').textContent = data.context.userId;
    document.getElementById('utouidfield').textContent = data.context.utouId;
    document.getElementById('groupidfield').textContent = data.context.groupId;
}

function sampleAjax(groupId, userId, displayName) {
    var request = new XMLHttpRequest();
    request.open('POST', '/content', false);
    request.send(JSON.stringify({
        groupId: groupId
    }));

    if (request.status === 200) {
        log(request.responseText);
        var response = JSON.parse(request.responseText);
        if (response['candidateDates'].length != 0) {
            var text = '<table style="margin: 0 auto;">';
            text += '<colgroup span="1" style="background-color: #CCFFCC;"/>';
            text += '<colgroup span="4" style="background-color: #FFFFCC;"/>';
            text += '<thead>';
            text += '<tr style="background-color: #CCFFFF;"><th>日時</th><th>出<br />席</th><th>遅<br />刻</th><th>欠<br />席</th><th>確<br />認<br />中</th></tr>';
            text += '</thead>';
            text += '<tbody>';
            for (let i = 0; i < response['candidateDates'].length; i++) {
                text += '<tr>';
                text += '<th>' + response['candidateDates'][i] + '</th>';
                text += '<td style="text-align: center;"><input type="radio" name="c' + i + '" value="attendance" /></td>';
                text += '<td style="text-align: center;"><input type="radio" name="c' + i + '" value="late" /></td>';
                text += '<td style="text-align: center;"><input type="radio" name="c' + i + '" value="absent" /></td>';
                text += '<td style="text-align: center;"><input type="radio" name="c' + i + '" value="checking" checked /></td>';
                text += '</tr>';
            }
            text += '</tbody></table>';
            text += '<input type="hidden" name="groupId" value="' + groupId + '">';
            text += '<input type="hidden" name="userId" value="' + userId + '">';
            text += '<input type="hidden" name="displayName" value="' + displayName + '">';
            text += '<input id="submit" type="submit" value="送信" />';
        }
        document.getElementById('syukketu').innerHTML = text;
        // LIFF アプリを閉じる
        // https://developers.line.me/ja/reference/liff/#liffclosewindow()
        document.getElementById('submit').addEventListener('click', function () {
            liff.closeWindow();
        });
    }
}

function log(body) {
    var request = new XMLHttpRequest();
    request.open('POST', '/log', false);
    request.send(body);
}
