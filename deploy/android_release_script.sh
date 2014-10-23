#!/bin/bash
# Release script to control release process
# https://docs.google.com/a/ooyala.com/document/d/1Fn01jrOflV8CizF0Zs4M2ZgBnX8ict1HE4gCo48KiwA/edit


task=$1
new_ticket_num=$2
release_date=
target_branch=""
subtask_name=""
subtask_number=""
ticket_update_result=""

function usage {
  echo "$0 <task> <options>"
  echo "" 
  echo "  tasks:"
  echo "    auto-release | -ar:                                     Finish everything up to first round testing subtask"
  echo ""
  echo "    clone-ticket | -ct:                                     Creates new ticket from stub ticket in JIRA"
  echo "" 
  echo "    file-doc-ticket | -fdt <ticket-id>:                     Generate a doc ticket for the current release by cloning the DOC-510"
  echo ""
  echo "    cut-branch | -cb <ticket-id>:                           Creates branch from master with name release/YYY-MM-DD"
  echo ""
  echo "    update-first-round-testing-task | -frt <ticket-id>:     Update the first round testing subtask by posting the related links and comments"
  echo ""
  echo "    cherry-pick-fixes | -cpf <ticket-id>:                   Generate a new RC for next round testing with the fix and update the next round testing ticket"
  echo ""
  echo "    update-second-round-testing-task | -srt <ticket-id>:    Update the second round testing subtask by posting the related links and commnets"
  echo ""
  echo "    publish-sdk-release | -pr <ticket-id>:                  Publish the current release" 
  echo ""
  echo "    email-after-release | -ear <ticket-id>:                 Send release email to TODO"
  echo "      args= <ticket-id> Number of master deploy Ticket; i.e. PBA-1234"
  exit 1
}


function clone_ticket {
  echo -e "\033[37mGenerate main release ticket"

  read -p "Enter release date in YYYY-MM-DD format: " release_date
  check_release_date_format
  echo -e "\033[37mCloning stub ticket PBA-664..."

  new_ticket_num=`ruby ~/repos/android-sdk/deploy/jira_clone.rb PBA-664 "Android SDK Release Ticket ${release_date}" true`
  check_ticket_number_format
  receiver="liusha.huang@ooyala.com" #TODO: playback-oncall
  body="New Release Ticket for ${new_ticket_num} is generated"
  subject="New Release Ticket for ${new_ticket_num} has been generated"
  echo ${body} | mail -s "${subject}" ${receiver}
  echo -e "\033[32mNew Release Ticket ${new_ticket_num} Created. Sent email to ${receiver}."
}

function file_release_doc_ticket {
  echo ""
  echo -e "\033[37mFile a DOCS ticket to update SDK integration guides"
  subtask_name="File a DOCS ticket to update SDK integration guides"
  fetch_release_info

  echo "Generating doc tickets for Android Release-${release_date}"

  create a new DOCS ticket to track documentation
  new_Android_docs_ticket=`ruby ~/repos/android-sdk/deploy/jira_clone.rb DOC-510 "Prepare for Android Release-${release_date}"`
  if ! [[ ${new_Android_docs_ticket} =~ ^[Dd][Oo][Cc]-[0-9]{1,}$ ]]; then
    1>&2 echo -e "\033[31mError: Fail to generate the DOC ticket"
    exit 1
  fi
  echo "new doc ticket: " ${new_Android_docs_ticket}
  echo "main task ticket: " ${new_ticket_num}

  ticket_update_result=`ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "File a DOCS ticket to update SDK integration guides" "
  Android-SDK Release Note Ticket: ${new_Android_docs_ticket}" closed`
  check_ticket_update_result

  receiver="liusha.huang@ooyala.com" #TODO: playback-oncall
  body="Make sure to post the update for this release in this DOC ticket"
  subject="New DOC Ticket ${new_Android_docs_ticket} has been generated."
  echo ${body} | mail -s "${subject}" ${receiver}
  echo -e "\033[32mNew DOC Ticket ${new_Android_docs_ticket} has been created and post on doc subtask. Sent email to ${receiver}."
}

function cut_branch {
  echo ""
  echo -e "\033[37mCreate RC and Tag Master"
  subtask_name="Create RC and Tag Master"
  fetch_release_info
  target_branch="master"
  check_if_in_clean_target_branch

  echo -e "\033[37mPublishing Android Release RC for Release-${release_date}..."
  ~/repos/android-sdk/script/android-sdk pub -rc -push
  check_build_result

  new_branch="release/${release_date}"
  git checkout -b releases/${release_date}
  git push -u origin releases/${release_date}
  ticket_update_result=`ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Create RC and Tag Master" "Branch ${new_branch} is created. Closing sub-task." closed`
  check_ticket_update_result
  
  echo -e "\033[32mPublished a new RC from master" 
}

function update_first_round_testing_task {
  echo ""
  echo -e "\033[37mFirst Round Testing"
  subtask_name="First Round Testing"
  fetch_release_info
  echo -e "\033[37mUpdating first round sub-task..."
  testing_ticket_url="https://jira.corp.ooyala.com/issues/?jql=Project%3D%20PBA%20AND%20labels%20%3D%20SDK-Deploy-${release_date}"

  comment="Testing ticket url: ${testing_ticket_url}"$'\n\n'"When you create new bugs, please leave them unassigned. DO NOT assign them to Michael Len!"

  ticket_update_result=`ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "First Round Testing" "${comment}" closed`
  check_ticket_update_result
  echo -e "\033[32mClosed first round testing ticket"
}

function cherry_pick_fixes {
  echo ""
  echo -e "\033[37mCherry pick fixes"
  subtask_name="Cherry Pick Fixes and Deploy From New Release Branch"
  fetch_release_info
  target_branch="release/${release_date}"  
  check_if_in_clean_target_branch
  subtask_name="Second Round Testing"
  get_subtask_number

  ~/repos/android-sdk/script/android-sdk pub -rc -push
  check_build_result

  echo -e "\033[32mGenerated a new RC for next round testing" 
  comment="Closing this first round testing ticket."$'\n'"Please take a look at our second round testing ticket: ${subtask_number} and please provide your feedback there."
  ticket_update_result=`ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Cherry Pick Fixes and Deploy From New Release Branch" "${comment}" closed`
  check_ticket_update_result
  echo -e "\033[32mUpdated cherry_pick_fixes ticket"
  update_second_round_testing_task
}

function update_second_round_testing_task {
  echo ""
  echo -e "\033[37mSecond Round Testing"
  subtask_name="Second Round Testing"
  fetch_release_info
  echo -e "\033[37mUpdating second round sub-task"
  testing_ticket_url="https://jira.corp.ooyala.com/issues/?jql=Project%3D%20PBA%20AND%20labels%20%3D%20SDK-Deploy-${release_date}"
  comment="${testing_ticket_url}"$'\n\n'"When you create new bugs, please leave them unassigned"
  ticket_update_result=`ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Second Round Testing" "Test tickets url: ${testing_ticket_url}" Investigating`
  check_ticket_update_result
  echo -e "\033[32mUpdated second round testing ticket"
}

function publish_sdk_release {
  echo ""
  echo -e "\033[37mPublish SDK Release"
  subtask_name="Publish SDK Releases"
  fetch_release_info
  target_branch="release/${release_date}"  
  check_if_in_clean_target_branch
  echo -e "\033[37mPublishing Adnroid-SDK/${release_date}"
  ~/repos/android-sdk/script/android-sdk pub -push
  check_build_result

  ticket_update_result=`ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Publish SDK Releases" "Adnroid-SDK/${release_date} is released" closed`
  check_ticket_update_result
  echo -e "\033[32mAdnroid-SDK/${release_date} published!"
}

function email_after_release {
  echo ""
  echo -e "\033[Email SDK Release"
  subtask_name="Email Announcement and Release Notes"
  fetch_release_info
  receiver="liusha.huang@ooyala.com"
  body="Native SDKs ${release_date} Released"
  subject="Native SDKs ${release_date} Released"
  echo ${body} | mail -s "${subject}" ${receiver}
  echo -e "\033[32mRelease email has been sent to ${receiver}."

  ticket_update_result=`ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Email Announcement and Release Notes" "Release email has been sent to ${receiver}." closed`
  check_ticket_update_result
}

function fetch_release_info { 
  echo "\033[37mFetching release info..."
  check_ticket_number_format
  can_move_on
  get_release_date
}

function check_if_in_clean_target_branch {
  echo "\033[37mChecking ${target_branch}..."
  if [[ "`git branch | sed -n '/\* /s///p'`" != "${target_branch}" ]]; then
    read -p "You are not on the ${target_branch} branch. Do you want to force to switch to ${target_branch} now? [Y/n]" switch
    if [ "${switch}" = "Y" ]; then
      git checkout -f ${target_branch}
    else
      exit 1
    fi

  else
    LOCAL=$(git rev-parse ${target_branch})
    REMOTE=$(git rev-parse origin/${target_branch})
    BASE=$(git merge-base ${target_branch} origin/${target_branch})

    if [ ${LOCAL} = ${REMOTE} ]; then
      if [[ $(git diff --shortstat 2> /dev/null | tail -n1) != "" ]]; then
        read -p "You are not on the ${target_branch} branch. Do you want to force to switch to ${target_branch} now? [Y/n]" clean
        if [ "${clean}" = "Y" ]; then
          git reset --hard
          git clean -f
        else
          exit 1
        fi
      fi

    elif [ ${LOCAL} = ${BASE} ]; then
        1>&2 echo "\033[31mError: Need to pull. Fetch and merge origin/master; then retry."
        exit 1
    elif [ ${REMOTE} = ${BASE} ]; then
        echo "\033[31mError: Need to push"
        exit 1
    else
        echo "\033[31mError: The branch is diverged."
        exit 1
    fi
  fi
}

function get_release_date {
  if [ "${release_date}" = "" ]; then
    echo "\033[37mTrying to get the ticket name for ${new_ticket_num}"

    release_date=`ruby ~/repos/android-sdk/deploy/jira_info.rb ${new_ticket_num} -n | sed "s/Android SDK Release Ticket //g"`
    check_release_date_format
  fi
}

function check_release_date_format {
  if ! [[ $release_date =~ ^20[1-9][0-9]-(0[1-9]|1[0-2])-(0[1-9]|1[0-9]|2[0-9]|3[0-1])$ ]]; then
    1>&2 echo -e "\033[31mError: Not a valid date format. Please make sure the main release ticket name is in correct format: Android SDK Release Ticket YYYY-MM-DD"
    exit 1
  fi
  echo -e "\033[32mGet release date = ${release_date}"
}

function can_move_on {
  echo "\033[37mChecking previous subtask status..."
  information=`ruby ~/repos/android-sdk/deploy/jira_info.rb ${new_ticket_num} --previous-steps-finished "${subtask_name}"`
  echo $information | sed "s/false/ /"
  can_move_on=`echo ${information} | awk '{ print $(NF) }'`
  if [[ ${can_move_on} != "true" ]]; then
    1>&2 echo -e "\033[31mError: have not finish previous subtaskes or current subtask is not open anymore"
    exit 1
  fi   
  echo -e "\033[32mAll previous steps have been finished and the current step has not been started yet. OK to move on!"
}

function get_subtask_number {
  echo "\033[37mFetching subtask number for ${subtask_name}"
  subtask_number=`ruby ~/repos/android-sdk/deploy/jira_info.rb ${new_ticket_num} -sn ${subtask_name}`
  check_ticket_number_format
}

function check_ticket_number_format {
  echo "\033[37mChecking ticket format: ${new_ticket_num}"
  if ! [[ ${new_ticket_num} =~ ^[Pp][Bb][Aa]-[0-9]{1,}$ ]]; then
    1>&2 echo -e "\033[31mError: Not a valid PBA ticket format."
    exit 1
  fi
  echo -e "\033[32mValid PBA ticket format."
}

function check_ticket_update_result {
  echo "\033[37mChecking ticket update result"
  update_result=`echo ${ticket_update_result} | tail -1`
  if [[ ${update_result} != "true" ]]; then
    1>&2 echo -e "\033[31mError: Fail to update the ticket"
    exit 1
  fi
}

function check_build_result {
  result=$?
  if [ $result -eq 1 ]; then
     1>&2 echo -e "\033[31mError: Fail to build RC"
    exit 1
  fi
}

function auto_release {
  clone_ticket
  file_release_doc_ticket
  cut_branch
  update_first_round_testing_task
}


case "$1" in
  auto-release | -ar) shift; auto_release $*;;
  clone-ticket | -ct) shift; clone_ticket $*;;
  file-doc-ticket | -fdt) shift; file_release_doc_ticket $*;;
  cut-branch | -cb) shift; cut_branch $*;;
  update-first-round-testing-task |-frt) shift; update_first_round_testing_task $*;;
  cherry-pick-fixes | -cpf) shift; cherry_pick_fixes $*;;
  update-second-round-testing-task| -srt) shift; update_second_round_testing_task $*;;
  publish-sdk-release | -pr) shift; publish_sdk_release $*;;
  email-after-release | -ear) shift; email_after_release $*;;
  *) usage;
esac


















