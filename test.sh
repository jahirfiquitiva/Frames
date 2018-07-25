printf "\n\nGetting tag information\n"
tagInfo="$(curl https://api.github.com/repos/jahirfiquitiva/Blueprint/releases/tags/1.2.5)"
releaseId="$(echo "$tagInfo" | jq ".id")"

releaseNameOrg="$(echo "$tagInfo" | jq --raw-output ".tag_name")"
releaseName=$(echo $releaseNameOrg | cut -d "\"" -f 2)

ln=$"%0D%0A"
tab=$"%09"

changesOrg="$(echo "$tagInfo" | jq --raw-output ".body")"
changes=$(echo $changesOrg | cut -d "\"" -f 2)
changes=$"$changes"

urlText="$(echo "$tagInfo" | jq --raw-output ".assets[].browser_download_url")"
url=$(echo $urlText | cut -d "\"" -f 2)
url=$"$url"

message=$"*New ${repoName} update available now!*${ln}*Version:*${ln}${tab}${releaseName}${ln}*Changes:*${ln}${changes}${ln}"
btns=$"{\"inline_keyboard\":[[{\"text\":\"How To Update\",\"url\":\"https://github.com/${TRAVIS_REPO_SLUG}/wiki/How-to-update\"}],[{\"text\":\"Download sample\",\"url\":\"${url}\"}]]}"

printf "\n\nSending message to Telegram channel\n\n"
echo "Message: $message"
printf "\n\n"
echo "Buttons: ${btns}"
printf "\n\n"

telegramUrl="https://api.telegram.org/bot${TEL_BOT_KEY}/sendMessage?chat_id=@JFsDashSupport&text=${message}&parse_mode=Markdown&reply_markup=${btns}"
echo "$telegramUrl"
printf "\n\n"
curl -g "${telegramUrl}"