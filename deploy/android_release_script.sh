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
build_script_result=""

green='\033[32m'
red='\033[31m'
white='\033[37m'

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
  echo "    email-after-release | -ear <ticket-id>:                 Send release email to playback@ooyala.com"
  echo "      args= <ticket-id> Number of master deploy Ticket; i.e. PBA-1234"
  exit 1
}

function clone_ticket {
  echo -e "${white}Generate main release ticket"

  read -p "Enter release date in YYYY-MM-DD format: " release_date
  check_release_date_format
  echo -e "${white}Cloning stub ticket PBA-664..."

  new_ticket_num=`ruby ~/repos/android-sdk/deploy/jira_clone.rb PBA-664 "Android SDK Release Ticket ${release_date}" true`
  check_ticket_number_format
  receiver="playback-app-oncall@ooyala.com"
  body="New Release Ticket for ${new_ticket_num} has been generated"
  subject="New Release Ticket for ${new_ticket_num} has been generated"
  echo ${body} | mail -s "${subject}" ${receiver}
  echo -e "${green}New Release Ticket ${new_ticket_num} Created. Sent email to ${receiver}."
}

function file_release_doc_ticket {
  echo ""
  echo -e "${white}File a DOCS ticket to update SDK integration guides"
  subtask_name="File a DOCS ticket to update SDK integration guides"
  fetch_release_info

  echo -e "${white}Generating doc tickets for Android Release-${release_date}"

  # create a new DOCS ticket to track documentation
  new_Android_docs_ticket=`ruby ~/repos/android-sdk/deploy/jira_clone.rb DOC-510 "Prepare for Android Release-${release_date}"`  
  echo -e "${white}New doc ticket: " ${new_Android_docs_ticket}
  if ! [[ ${new_Android_docs_ticket} =~ ^[Dd][Oo][Cc]-[0-9]{1,}$ ]]; then
    1>&2 echo -e "${red}Error: Fail to generate the DOC ticket"
    exit 1
  fi

  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "File a DOCS ticket to update SDK integration guides" "Android-SDK Release Note Ticket: ${new_Android_docs_ticket}" closed
  ticket_update_result=$?
  check_ticket_update_result

  receiver="playback-app-oncall@ooyala.com"
  body="Make sure to post the update for this release in this DOC ticket"
  subject="New DOC Ticket ${new_Android_docs_ticket} has been generated."
  echo ${body} | mail -s "${subject}" ${receiver}
  echo -e "${green}New DOC Ticket ${new_Android_docs_ticket} has been created and post on doc subtask. Sent email to ${receiver}."
}

function cut_branch {
  echo ""
  echo -e "${white}Create RC and Tag Master"
  subtask_name="Create RC and Tag Master"
  fetch_release_info
  target_branch="master"
  check_if_in_clean_target_branch

  echo -e "${white}"
  read -p "Enter release version: " release_version
  check_release_version_format ${release_version}

  echo -e "${white}"
  read -p "Enter RC number: " rc_number
  check_rc_number_format ${rc_number}

  echo -e "${white}Publishing Android Release RC for Release-${release_date}..."
  ~/repos/android-sdk/script/android-sdk pub -rc${rc_number} -v${release_version} -push
  build_script_result=$?
  check_build_result

  new_branch="release/${release_date}"
  git checkout -b releases/${release_date}
  git push -u origin releases/${release_date}
  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Create RC and Tag Master" "Branch ${new_branch} is created. Closing sub-task." closed
  ticket_update_result=$?
  check_ticket_update_result
  
  echo -e "${green}Published a new RC from master" 
}

function update_first_round_testing_task {
  echo ""
  echo -e "${white}First Round Testing"
  subtask_name="First Round Testing"
  fetch_release_info
  echo -e "${white}Updating first round sub-task..."
  testing_ticket_url="https://jira.corp.ooyala.com/issues/?jql=Project%3D%20PBA%20AND%20labels%20%3D%20SDK-Deploy-${release_date}"

  comment="Testing ticket url: ${testing_ticket_url}"$'\n\n'"When you create new bugs, please leave them unassigned. DO NOT assign them to Michael Len!"

  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "First Round Testing" "${comment}" Investigating
  ticket_update_result=$?
  check_ticket_update_result
  echo -e "${green}Investigating first round testing ticket"
}

function cherry_pick_fixes {
  echo ""
  echo -e "${white}Cherry pick fixes"
  subtask_name="Cherry Pick Fixes and Deploy From New Release Branch"
  fetch_release_info
  target_branch="release/${release_date}"  
  check_if_in_clean_target_branch
  subtask_name="Second Round Testing"
  get_subtask_number

  ~/repos/android-sdk/script/android-sdk pub -rc -push
  build_script_result=$?
  check_build_result

  echo -e "${green}Generated a new RC for next round testing" 
  comment="Closing this first round testing ticket."$'\n'"Please take a look at our second round testing ticket: ${subtask_number} and please provide your feedback there."
  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Cherry Pick Fixes and Deploy From New Release Branch" "${comment}" closed
  ticket_update_result=$?
  check_ticket_update_result
  echo -e "${green}Updated cherry_pick_fixes ticket"
  update_second_round_testing_task
}

function update_second_round_testing_task {
  echo ""
  echo -e "${white}Second Round Testing"
  subtask_name="Second Round Testing"
  fetch_release_info
  echo -e "${white}Updating second round sub-task"
  testing_ticket_url="https://jira.corp.ooyala.com/issues/?jql=Project%3D%20PBA%20AND%20labels%20%3D%20SDK-Deploy-${release_date}"
  comment="${testing_ticket_url}"$'\n\n'"When you create new bugs, please leave them unassigned"
  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Second Round Testing" "Test tickets url: ${testing_ticket_url}" Investigating
  ticket_update_result=$?
  check_ticket_update_result
  echo -e "${green}Updated second round testing ticket"
}

function publish_sdk_release {
  echo ""
  echo -e "${white}Publish SDK Release"
  subtask_name="Publish SDK Releases"
  fetch_release_info
  target_branch="release/${release_date}"  
  check_if_in_clean_target_branch
  echo -e "${white}Publishing Adnroid-SDK/${release_date}"
  ~/repos/android-sdk/script/android-sdk pub -push
  build_script_result=$?
  check_build_result

  ticket_update_result=`ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Publish SDK Releases" "Adnroid-SDK/${release_date} is released" closed`
  check_ticket_update_result
  echo -e "${green}Adnroid-SDK/${release_date} published!"
}

function email_after_release {
  echo ""
  echo -e "${white}Email SDK Release"
  subtask_name="Email Announcement and Release Notes"
  fetch_release_info
  receiver="playback@ooyala.com"
  body="Native SDKs ${release_date} has been Released"
  subject="Native SDKs ${release_date} has been Released"
  echo ${body} | mail -s "${subject}" ${receiver}
  echo -e "${green}Release email has been sent to ${receiver}."

  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Email Announcement and Release Notes" "Release email has been sent to ${receiver}." closed
  ticket_update_result=$?
  check_ticket_update_result
}

function fetch_release_info { 
  echo -e "${white}Fetching release info..."
  check_ticket_number_format
  can_move_on
  get_release_date
}

function check_if_in_clean_target_branch {
  echo -e "${white}Checking ${target_branch}..."
  if [[ "`git rev-parse --abbrev-ref HEAD`" != "${target_branch}" ]]; then
      exit 1
  else
    LOCAL=$(git rev-parse ${target_branch})
    REMOTE=$(git rev-parse origin/${target_branch})
    BASE=$(git merge-base ${target_branch} origin/${target_branch})
    if [ ${LOCAL} = ${REMOTE} ]; then
      if [[ $(git diff --shortstat 2> /dev/null | tail -n1) != "" ]]; then
          exit 1
      fi
    elif [ ${LOCAL} = ${BASE} ]; then
        1>&2 echo -e "${red}Error: Need to pull. Fetch and merge origin/master; then retry."
        exit 1
    elif [ ${REMOTE} = ${BASE} ]; then
        echo -e "${red}Error: Need to push"
        exit 1
    else
        echo -e "${red}Error: The branch is diverged."
        exit 1
    fi
  fi
}

function get_release_date {
  if [ "${release_date}" = "" ]; then
    echo -e "${white}Trying to get the ticket name for ${new_ticket_num}"
    release_date=`ruby ~/repos/android-sdk/deploy/jira_info.rb ${new_ticket_num} -n | sed "s/Android SDK Release Ticket //g"`
    check_release_date_format
  fi
}

function check_release_version_format () {
    if ! [[ $1 =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    1>&2 echo -e "${red}Error: Not a valid release version (expected xx.xx.xx)."
    exit 1
  fi
  echo -e "${green}Get release version = $1"
}

function check_rc_number_format () {
    if ! [[ $1 =~ ^[0-9]+$ ]]; then
    1>&2 echo -e "${red}Error: Not a valid rc number (expected any positive integer)."
    exit 1
  fi
  echo -e "${green}Get rc number  = $1"
}

function check_release_date_format {
  if ! [[ $release_date =~ ^20[1-9][0-9]-(0[1-9]|1[0-2])-(0[1-9]|1[0-9]|2[0-9]|3[0-1])$ ]]; then
    1>&2 echo -e "${red}Error: Not a valid date format. Please make sure the main release ticket name is in correct format: Android SDK Release Ticket YYYY-MM-DD"
    exit 1
  fi
  echo -e "${green}Get release date = ${release_date}"
}

function can_move_on {
  echo -e "${white}Checking previous subtask status..."
  information=`ruby ~/repos/android-sdk/deploy/jira_info.rb ${new_ticket_num} --previous-steps-finished "${subtask_name}"`
  echo $information | sed "s/false/ /"
  can_move_on=`echo ${information} | awk '{ print $(NF) }'`
  if [[ ${can_move_on} != "true" ]]; then
    1>&2 echo -e "${red}Error: have not finish previous subtaskes or current subtask is not open anymore"
    exit 1
  fi   
  echo -e "${green}All previous steps have been finished and the current step has not been started yet. OK to move on!"
}

function get_subtask_number {
  echo -e "${white}Fetching subtask number for ${subtask_name}"
  subtask_number=`ruby ~/repos/android-sdk/deploy/jira_info.rb ${new_ticket_num} -sn ${subtask_name}`
  check_ticket_number_format
}

function check_ticket_number_format {
  echo -e "${white}Checking ticket format: ${new_ticket_num}"
  if ! [[ ${new_ticket_num} =~ ^[Pp][Bb][Aa]-[0-9]{1,}$ ]]; then
    1>&2 echo -e "${red}Error: Not a valid PBA ticket format."
    exit 1
  fi
  echo -e "${green}Valid PBA ticket format."
}

function check_ticket_update_result {
  if [ $ticket_update_result -ne 0 ]; then
     1>&2 echo -e "${red}Error: Fail to update ticket"
    exit 1
  fi
}

function check_build_result {
  if [ $build_script_result -eq 1 ]; then
     1>&2 echo -e "${red}Error: Fail to build RC"
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

  










