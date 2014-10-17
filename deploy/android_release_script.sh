#!/bin/bash
# Release script to control release process
# https://docs.google.com/a/ooyala.com/document/d/1Fn01jrOflV8CizF0Zs4M2ZgBnX8ict1HE4gCo48KiwA/edit


task=$1
new_ticket_num=$2
release_date=
target_branch=""
subtask_name=""
subtask_number=""

function usage {
  echo "$0 <task> <options>"
  echo "" 
  echo "  tasks:"
  echo "    auto-release | -ar:                                     Finish everything up to first round testing subtask"
  echo ""
  echo "    clone-ticket | -ct:                                     Creates new ticket from stub ticket in JIRA"
  echo "" 
  echo "    cut-branch | -cb <ticket-id>:                           Creates branch from master with name release/YYY-MM-DD"
  echo ""
  echo "    file-doc-ticket | -fdt <ticket-id>:                     Generate a doc ticket for the current release by cloning the DOC-510"
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
  echo ""
  echo "Generate main release ticket"

  read -p "Enter release date in YYYY-MM-DD format: " release_date
  check_release_date_format
  echo "Cloning stub ticket PBA-664..."

  new_ticket_num=`ruby ~/repos/android-sdk/deploy/jira_clone.rb PBA-664 "Android SDK Release Ticket ${release_date}" true`
  check_ticket_number_format
  receiver="liusha.huang@ooyala.com" #TODO: playback-oncall
  body="JIRA Deployment ticket created"
  subject="New Cloned Ticket for ${new_ticket_num}"
  echo ${body} | mail -s "${subject}" ${receiver}
  echo -e "\033[32mNew Ticket ${new_ticket_num} Created. Sent email to ${receiver}."
}

function file_release_doc_ticket {
  echo ""
  echo "File a DOCS ticket to update SDK integration guides"
  subtask_name="File a DOCS ticket to update SDK integration guides"
  fetch_release_info

  echo "Generating doc tickets for Android Release-${release_date}"

  #create a new DOCS ticket to track documentation
  new_Android_docs_ticket=`ruby ~/repos/android-sdk/deploy/jira_clone.rb DOC-510 "Prepare for Android Release-${release_date}"`
  if ! [[ ${new_Android_docs_ticket} =~ ^[Dd][Oo][Cc]-[0-9]{1,}$ ]]; then
    echo -e "\033[31mFail to generate the DOC ticket"
    exit 1
  fi
  echo "new doc ticket: " ${new_Android_docs_ticket}
  echo "main task ticket: " ${new_ticket_num}

  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "File a DOCS ticket to update SDK integration guides" "
  Android-SDK Release Note Ticket: ${new_Android_docs_ticket}" closed
  
  receiver="liusha.huang@ooyala.com" #TODO: playback-oncall
  body="Make sure to post the update for this release in this DOC ticket"
  subject="New DOC Ticket ${new_Android_docs_ticket} has been generated."
  echo ${body} | mail -s "${subject}" ${receiver}
  echo -e "\033[32mNew DOC Ticket ${new_Android_docs_ticket} has been created and post on doc subtask. Sent email to ${receiver}."
}

function cut_branch {
  echo ""
  echo "Create RC and Tag Master"
  subtask_name="Create RC and Tag Master"
  fetch_release_info
  target_branch="master"
  check_if_in_clean_target_branch

  echo "Publishing Android Release RC for Release-${release_date}..."
  ~/repos/android-sdk/script/android-sdk pub -rc -push

  new_branch="release/${release_date}"
  git checkout -b releases/${release_date}
  git push -u origin releases/${release_date}
  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Create RC and Tag Master" "Branch ${new_branch} is created. Closing sub-task." closed  
  echo -e "\033[32mPublished a new RC from master" 
}

function update_first_round_testing_task {
  echo ""
  echo "First Round Testing"
  subtask_name="First Round Testing"
  fetch_release_info
  echo "Updating first round sub-task..."
  testing_ticket_url="https://jira.corp.ooyala.com/issues/?jql=Project%3D%20PBA%20AND%20labels%20%3D%20SDK-Deploy-${release_date}"

  comment="Testing ticket url: ${testing_ticket_url}"$'\n\n'"When you create new bugs, please leave them unassigned. DO NOT assign them to Michael Len!"

  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "First Round Testing" "${comment}" closed
  echo -e "\033[32mClosed first round testing ticket"
}

function cherry_pick_fixes {
  echo ""
  echo "Cherry pick fixes"
  subtask_name="Cherry Pick Fixes and Deploy From New Release Branch"
  fetch_release_info
  target_branch="release/${release_date}"  
  check_if_in_clean_target_branch
  subtask_name="Second Round Testing"
  get_subtask_number

  comment="Closing this first round testing ticket."$'\n'"Please take a look at our second round testing ticket: ${subtask_number} and please provide your feedback there."
  echo "${comment}"
  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Cherry Pick Fixes and Deploy From New Release Branch" "${comment}" closed    
  update-second-round-testing-task
  echo -e "\033[32mUpdated cherry_pick_fixes ticket"

  ~/repos/android-sdk/script/android-sdk pub -rc -push
  echo -e "\033[32mGenerated a new RC for next round testing" 
  update_second_round_testing_task
}

function update_second_round_testing_task {
  echo ""
  echo "Second Round Testing"
  subtask_name="Second Round Testing"
  fetch_release_info
  echo "Updating second round sub-task"
  testing_ticket_url="https://jira.corp.ooyala.com/issues/?jql=Project%3D%20PBA%20AND%20labels%20%3D%20SDK-Deploy-${release_date}"
  comment="${testing_ticket_url}"$'\n\n'"When you create new bugs, please leave them unassigned"
  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Second Round Testing" "Test tickets url: ${testing_ticket_url}" Investigating
  echo -e "\033[32mUpdated second round testing ticket"
}

function publish_sdk_release {
  subtask_name="Publish SDK Releases"
  fetch_release_info
  target_branch="release/${release_date}"  
  check_if_in_clean_target_branch
  echo "Publishing Adnroid-SDK/${release_date}"
  ~/repos/android-sdk/script/android-sdk pub -push
  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Publish SDK Releases" "Adnroid-SDK/${release_date} is released" closed
  echo -e "\033[32mAdnroid-SDK/${release_date} published!"
}

function email_after_release {
  subtask_name="Email Announcement and Release Notes"
  fetch_release_info
  receiver="liusha.huang@ooyala.com"
  body="Native SDKs ${release_date} Released"
  subject="Native SDKs ${release_date} Released"
  echo ${body} | mail -s "${subject}" ${receiver}
  echo -e "\033[32mRelease email has been sent to ${receiver}."

  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Email Announcement and Release Notes" "Release email has been sent to ${receiver}." closed
}

function fetch_release_info { 
  echo "Fetching release info..."
  check_ticket_number_format
  can_move_on
  get_release_date
}

function check_if_in_clean_target_branch {
  echo "Checking ${target_branch}..."
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
      echo "Up-to-date"
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
        echo "Need to pull. Fetch and merge origin/master; then retry."
        exit 1
    elif [ ${REMOTE} = ${BASE} ]; then
        echo "Need to push"
        exit 1
    else
        echo "Diverged. Error."
        exit 1
    fi
  fi
}

function get_release_date {
  if [ "${release_date}" = "" ]; then
    echo "Trying to get the ticket name for ${new_ticket_num}"

    release_date=`ruby ~/repos/android-sdk/deploy/jira_info.rb ${new_ticket_num} -n | sed "s/Android SDK Release Ticket //g"`
    check_release_date_format
  fi
}

function check_release_date_format {
  if ! [[ $release_date =~ ^20[1-9][0-9]-(0[1-9]|1[0-2])-(0[1-9]|1[0-9]|2[0-9]|3[0-1])$ ]]; then
    echo -e "\033[31mNot a valid date format. Please make sure the main release ticket name is in correct format: Android SDK Release Ticket YYYY-MM-DD"
    exit 1
  fi
  echo -e "\033[32mGet release date = ${release_date}"
}

function can_move_on {
  echo "Checking previous subtask status..."
  information=`ruby ~/repos/android-sdk/deploy/jira_info.rb ${new_ticket_num} --previous-steps-finished "${subtask_name}"`
  echo $information | sed "s/false/ /"
  can_move_on=`echo ${information} | awk '{ print $(NF) }'`
  if [[ ${can_move_on} != "true" ]]; then
    echo -e "\033[31mFail: have not finish previous subtaskes or current subtask is not open anymore"
    exit 1
  fi   
  echo -e "\033[32mAll previous steps have been finished and the current step has not been started yet. OK to move on!"
}

function get_subtask_number {
  echo "Fetching subtask number for ${subtask_name}"
  subtask_number=`ruby ~/repos/android-sdk/deploy/jira_info.rb ${new_ticket_num} -sn ${subtask_name}`
}

function check_ticket_number_format {
  echo "Checking ticket format: ${new_ticket_num}"
  if ! [[ ${new_ticket_num} =~ ^[Pp][Bb][Aa]-[0-9]{1,}$ ]]; then
    echo -e "\033[31mNot a valid PBA ticket format."
    exit 1
  fi
  echo -e "\033[32mValid PBA ticket format."
}

function auto_release {
  clone_ticket
  file_release_doc_ticket
  cut_branch
  update_first_round_testing_task
}


case "$1" in
  auto-release | -ar) auto_release shift;
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


















