#!/bin/bash
# Release script to control release process
# https://docs.google.com/a/ooyala.com/document/d/1Fn01jrOflV8CizF0Zs4M2ZgBnX8ict1HE4gCo48KiwA/edit


task=$1
new_ticket_num=$2
release_date=$2
target_branch=""
subtask_name=""
subtask_number=""

function usage {
  echo "$0 <task> <options>"
  echo "" 
  echo "  tasks:"
  echo "    release-all | -ra:                                      Finish everything up to first round testing subtask"
  echo ""
  echo "    clone-ticket | -ct:                                     Creates new ticket from stub ticket in JIRA"
  echo "" 
  echo "    cut-branch | -cb <ticket-id>:                           Creates branch from master with name release/YYY-MM-DD"
  echo ""
  echo "    file-doc-ticket | fdt <ticket-id>:                      Generate a doc ticket for the current release by cloning the DOC-510"
  echo ""
  echo "    update-first-round-testing-task | frt <ticket-id>:      Update the first round testing subtask by posting the related links and comments"
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

#TODO: OK Vs. Error and color

function clone_ticket {
  # version number
  read -p "Enter release date in YYYY-MM-DD format: " release_date
  echo "Cloning stub ticket PBA-664..."
  check_release_date_format

  # comment for now so we don't create a million test tickets in JIRA
  new_ticket_num=`ruby ~/repos/android-sdk/deploy/jira_clone.rb PBA-664 "Android SDK Release Ticket ${release_date}" true` #TODO: update name for pject sproecific
  # TODO: check the failure
  echo "${release_script_dir}jira_clone.rb"
  receiver="liusha.huang@ooyala.com" #TODO: playback-oncall
  body="JIRA Deployment ticket created"
  subject="New Cloned Ticket for ${new_ticket_num}"
  echo ${body} | mail -s "${subject}" ${receiver}
  echo -e "\033[32m New Ticket ${new_ticket_num} Created. Sent email to ${receiver}."
}

function file_release_doc_ticket {
  subtask_name="File a DOCS ticket to update SDK integration guides"
  fetch_release_info

  echo "Generating doc tickets for Android Release-${release_date}"

  #create a new DOCS ticket to track documentation
  new_Android_docs_ticket=`ruby ~/repos/android-sdk/deploy/jira_clone.rb DOC-510 "Prepare for Android Release-${release_date}"`
  echo "new doc ticket: " ${new_Android_docs_ticket}
  echo "main task ticket: " ${new_ticket_num}
  #add the new doc tickets to the comments of the doc sub-task of the main release tickets
  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "File a DOCS ticket to update SDK integration guides" "
  Android-SDK Release Note Ticket: ${new_Android_docs_ticket}" closed
  
  # TODO: email about the new doc ticket => oncall

  echo -e "\033[32m ${new_Android_docs_ticket} has been generated and post on doc subtask"
}

function cut_branch {
  subtask_name="Create RC and Tag Master"
  fetch_release_info
  target_branch="master"
  check_if_in_clean_target_branch

  echo "Publishing Android Release RC for Release-${release_date}"
  ~/repos/android-sdk/script/android-sdk pub -rc -push

  new_branch = "releases/${release_date}"
  git checkout -b releases/${release_date}
  git push -u origin releases/${release_date}
  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Create RC and Tag Master" "Branch ${new_branch} created. Closing sub-task." closed  
  echo -e "\033[32m Published a new RC from master" 
}

function update_first_round_testing_task {
  subtask_name="First Round Testing"
  fetch_release_info
  echo "Updating first round sub-task..."
  testing_ticket_url="https://jira.corp.ooyala.com/issues/?jql=Project%3D%20PBA%20AND%20labels%20%3D%20SDK-Deploy-${release_date}"

  comment="Testing ticket url: ${testing_ticket_url}"$'\n\n'"When you create new bugs, please leave them unassigned"
  echo "${comment}"
  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "First Round Testing" "${comment}" closed
  echo -e "\033[32m Updated first round testing ticket"
}

function cherry_pick_fixes {
  subtask_name="Cherry Pick Fixes and Deploy From New Release Branch"
  fetch_release_info
  target_branch="release/${release_date}"  

  subtask_name="Second Round Testing"
  get_subtask_number
  echo "${subtask_number}"

  comment="Closing this first round testing ticket."$'\n'"Please take a look at our second round testing ticket: ${subtask_number} and please provide your feedback there."
  echo "${comment}"
  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Cherry Pick Fixes and Deploy From New Release Branch" "${comment}" closed    
  update-second-round-testing-task
  echo -e "\033[32m Updated cherry_pick_fixes ticket"


  ~/repos/android-sdk/script/android-sdk pub -rc -push
  echo -e "\033[32m Generated a new RC for next round testing" 
}

function update_second_round_testing_task {
  subtask_name="Second Round Testing"
  fetch_release_info
  echo "Updating second round sub-task"
  testing_ticket_url="https://jira.corp.ooyala.com/issues/?jql=Project%3D%20PBA%20AND%20labels%20%3D%20SDK-Deploy-${release_date}"
  comment="${testing_ticket_url}"$'\n\n'"When you create new bugs, please leave them unassigned"
  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Second Round Testing" "Test tickets url: ${testing_ticket_url}" Investigating
  echo -e "\033[32m Updated second round testing ticket"
}

function publish_sdk_release {
  subtask_name="Publish SDK Releases"
  fetch_release_info
  target_branch="release/${release_date}"  
  check_if_in_clean_target_branch
  echo "Publishing Adnroid-SDK/${release_date}"
  ~/repos/android-sdk/script/android-sdk pub -push
  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Publish SDK Releases" "Adnroid-SDK/${release_date} is released" closed
  echo -e "\033[32m Adnroid-SDK/${release_date} published!"
}

function email_after_release {
  subtask_name="Email Announcement and Release Notes"
  fetch_release_info
  receiver="liusha.huang@ooyala.com"
  body="Native SDKs ${release_date} Released"
  subject="Native SDKs ${release_date} Released"
  echo ${body} | mail -s "${subject}" ${receiver}
  echo -e "\033[32m Release email has been sent to ${receiver}."

  ruby ~/repos/android-sdk/deploy/jira_update.rb "${new_ticket_num}" "Email Announcement and Release Notes" "Release email has been sent to ${receiver}." closed
}

function fetch_release_info { 
  if [ task != "-ct" ] && [ task != "clone-ticket" ]; then #TODO: no if
    echo "fetching release info..."
    check_ticket_number_format
    can_move_on
    get_release_date
  fi
}

function check_if_in_clean_target_branch {
  # TODO: ask for checkout the correct branch and clean the branch
  echo "Checking ${target_branch}..."
  if [[ "`git branch | sed -n '/\* /s///p'`" != "${target_branch}" ]]; then
    echo "You are not on the ${target_branch} branch. Please switch to ${target_branch} and retry."
    exit 1
  else
    LOCAL=$(git rev-parse ${target_branch})
    REMOTE=$(git rev-parse origin/${target_branch})
    BASE=$(git merge-base ${target_branch} origin/${target_branch})

    if [ ${LOCAL} = ${REMOTE} ]; then
      echo "Up-to-date"
      if [[ $(git diff --shortstat 2> /dev/null | tail -n1) != "" ]]; then
        echo "But your git local repo is DIRTY. You need to commit."
        exit 1
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
    echo -e "\033[32m Get release date = ${release_date}"
  fi
}

function check_release_date_format {
  if ! [[ $release_date =~ ^20[1-9][0-9]-(0[1-9]|1[0-2])-(0[1-9]|1[0-9]|2[0-9]|3[0-1])$ ]]; then
    echo -e "\033[31m Not a valid date format. Please make sure the main release ticket name is in correct format: Android SDK Release Ticket YYYY-MM-DD"
    exit 1
  fi
  echo -e "\033[32m Get release date = ${release_date}"
}

function can_move_on {
  echo "Checking previous subtask status..."
  information=`ruby ~/repos/android-sdk/deploy/jira_info.rb ${new_ticket_num} --previous-steps-finished "${subtask_name}"`
  echo $information | sed "s/false/ /"
  can_move_on=`echo ${information} | awk '{ print $(NF) }'`
  if [[ ${can_move_on} != "true" ]]; then
    echo -e "\033[31m Finish previous steps before moving on"
    exit 1
  fi   
  echo -e "\033[32m All previous steps have been finished and the current step has not been started yet. OK to move on!"
}

function get_subtask_number {
  echo "Fetching subtask number for ${subtask_name}"
  subtask_number=`ruby ~/repos/android-sdk/deploy/jira_info.rb ${new_ticket_num} -sn ${subtask_name}`
}

function check_ticket_number_format {
  echo "checking ${new_ticket_num}"
  if ! [[ ${new_ticket_num} =~ ^[Pp][Bb][Aa]-[0-9]{1,}$ ]]; then
    echo -e "\033[31m Not a valid PBA ticket format. Please retry with ticket format PBA-XXX."
    exit 1
  fi
  echo -e "\033[32m Valid PBA ticket format."
}

function release_all {
  clone_ticket
  file_release_doc_ticket
  update_first_round_testing_task
  cut_branch
}


case "$1" in

  test) shift; check_release_date_format $*;;
  # release-all | -ra) shift;
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


















