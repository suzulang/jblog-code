function loadUserAvatar() {
    jQuery.ajax({
        url: "/user/getPhotoLink",  // 替换为实际获取头像 URL 的接口
        type: "POST",
        success: function(res) {
            if (res.data) {
                jQuery(".userAvatar").attr("src", res.data);
            }
        },
        error: function() {
            console.error("Failed to load user avatar");
        }
    });
}
