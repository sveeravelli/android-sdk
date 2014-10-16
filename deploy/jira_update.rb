require 'rubygems'
require 'net/https'
require 'json'

$jira_url = "https://jira.corp.ooyala.com"
$jira_path = "rest/api/latest/issue/"
issue_key = ARGV[0]
step = ARGV[1]
comment = ARGV[2]
operation = ARGV[3]  
$json_ext = ".json"
uri = URI.parse($jira_url + "/" + $jira_path + issue_key + $json_ext)

#USAGE BLOCK
if (issue_key == "--help" || issue_key == "-h")
  puts 'USAGE: ruby jira_update.rb [TICKET REF] "[STEP TITLE]" "[COMMENT]" EndState(Optional)'
  puts ''
  puts 'Example: ruby jira_update.rb RM-1359 "Cut Branch" "releases/prod/2014-01-14" closed'
  puts '         will find the child of RM-1359 with the Summary matching title "Cut Branch" '
  puts '         will add a comment of "release/prod/2014-01-14" to the ticket'
  puts '         will close the ticket'
  puts ''
  puts 'See README.md for more information'
  puts ''
  exit 0  
end

def get_available_transitions(issue_key, transition)
  uri = URI.parse($jira_url + "/" + $jira_path + issue_key + "/transitions?transitions.fields")
  http = Net::HTTP.new(uri.host, uri.port)
  http.use_ssl = true
  http.verify_mode = OpenSSL::SSL::VERIFY_NONE
  
  request = Net::HTTP::Get.new(uri.request_uri)
  request.basic_auth("jira-rest-api", "jira`rest`4p1")
  response = http.request(request)
  
  if response.code =~ /20[0-9]{1}/
    result = JSON.parse(response.body)
    result["transitions"].each do |available_transition|
      if available_transition["to"]["name"].downcase == transition.downcase
        return available_transition["id"]
      end
    end
    raise StandardError, "\n-----\nUnable to transition using " + transition + " for issue " +
      issue_key + "\n" + "Response :\n" + response.body + "\n-----"
  else
    raise StandardError, "\n-----\nUnable to retrieve transitions for issue " +
      issue_key + "\n" + "Response :\n" + response.body + "\n-----"
  end
  exit -1   #ERRORS OUT
end

def comment_issue(issue_key, comment)
  ticket_data = {}
  ticket_data["body"] = comment

  uri = URI.parse($jira_url + "/" + $jira_path + issue_key + "/comment")
  http = Net::HTTP.new(uri.host, uri.port)
  http.use_ssl = true
  http.verify_mode = OpenSSL::SSL::VERIFY_NONE
  
  request = Net::HTTP::Post.new(uri.request_uri, initheader = {'Content-Type' =>'application/json'})
  request.basic_auth("jira-rest-api", "jira`rest`4p1")
  request.body = ticket_data.to_json
  response = http.request(request)
  if response.code =~ /20[0-9]{1}/
    result = JSON.parse(response.body)
    return result
  end
  raise StandardError, "\n-----\nUnable to add Comment " + 
    response.code + " for issue " + issue_key +"\n"+"Response :\n" + 
    response.body + "\n-----"
  exit -1   #ERRORS OUT
end

def find_child(issue_key, step)
  if step == ""
    return issue_key
  end
  uri = URI.parse($jira_url + "/" + $jira_path + issue_key + $json_ext)
  http = Net::HTTP.new(uri.host, uri.port)
  http.use_ssl = true
  http.verify_mode = OpenSSL::SSL::VERIFY_NONE

  request = Net::HTTP::Get.new(uri.request_uri)
  request.basic_auth("jira-rest-api", "jira`rest`4p1")
  response = http.request(request)

  #response = Net::HTTP.get_response(URI.parse(jira_url + issue_key + json_ext))
  if response.code =~ /20[0-9]{1}/
    data = JSON.parse(response.body)

    if (data["fields"]["subtasks"].size > 0)
      data["fields"]["subtasks"].each do |subtask|
          subtask_key = subtask["key"]
          if subtask["fields"]["summary"].include? step
            return subtask_key
          end
      end
      raise StandardError, "\nCould not find step : " + step + " for issue " + issue_key
    end
    raise StandardError, "\nCould not find children for issue : " + issue_key
  else
    raise StandardError, "\n-----\nUnsuccessful response code " + 
      response.code + " for issue " + issue_key +"\n"+"Response :\n" + 
      response.body + "\n-----"
  end
  exit -1   #ERRORS OUT
end


def transition_issue(issue_key, ticket_data, transition)
  unless transition.nil?
    uri = URI.parse($jira_url + "/" + $jira_path + issue_key + "/transitions")
    http = Net::HTTP.new(uri.host, uri.port)
    http.use_ssl = true
    http.verify_mode = OpenSSL::SSL::VERIFY_NONE

    transition_id = get_available_transitions(issue_key, transition)

    ticket_data["transition"] = {"id" => transition_id}

    puts "\nTransitioning Issue: " + issue_key + " as " + transition + " using transition: " + transition_id
    request = Net::HTTP::Post.new(uri.request_uri, initheader = {'Content-Type' =>'application/json'})
    request.basic_auth("jira-rest-api", "jira`rest`4p1")
    request.body = ticket_data.to_json
    response = http.request(request)
    if response.code =~ /20[0-9]{1}/
      return true
    else
      raise StandardError, "\n-----\nUnsuccessful response code " + 
        response.code + " for issue " + issue_key + "\n" + "Response :\n" + 
        response.body + "\n-----"
    end
    return false
  end
  puts "\nNo transition for issue: " + issue_key
  return true
end

def get_issue_data(issue_key, uri)
  http = Net::HTTP.new(uri.host, uri.port)
  http.use_ssl = true
  http.verify_mode = OpenSSL::SSL::VERIFY_NONE
  request = Net::HTTP::Get.new(uri.request_uri)
  request.basic_auth("jira-rest-api", "jira`rest`4p1")
  response = http.request(request)

  #response = Net::HTTP.get_response(URI.parse(jira_url + issue_key + json_ext))
  if response.code =~ /20[0-9]{1}/
    data = JSON.parse(response.body)
    newdata = {}
    newfields = {}
    
    newdata["project"] = data["fields"]["project"]
    newdata["issuetype"] = data["fields"]["issuetype"]
    newfields["fields"] = newdata
  else
    raise StandardError, "\n-----\nUnsuccessful response code " + 
      response.code + " for issue " + issue_key + "\n" + "Response :\n" + 
      response.body + "\n-----"
  end
  newfields
end

ticket_key = find_child(issue_key, step)
comment_issue(ticket_key, comment)
ticket_data = get_issue_data(ticket_key, uri)
unless transition_issue(ticket_key, ticket_data, operation) 
  exit -1
end
exit 0