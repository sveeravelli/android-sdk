require 'rubygems'
require 'net/https'
require 'json'

$jira_url = "https://jira.corp.ooyala.com"
$jira_path = "rest/api/latest/issue/"
issue = ARGV[0]
summary = ARGV[1]
clone_subtasks = (ARGV[2] == "true" || (ARGV[2].nil?))
$json_ext = ".json"
uri = URI.parse($jira_url + "/" + $jira_path + issue + $json_ext)

#USAGE BLOCK
if (issue == "--help" || issue == "-h")
	puts 'USAGE: ruby jira_clone.rb [TICKET REF] "[NEW TITLE]" [CLONE SUBTICKETS (true or false)]'
	puts ''
	puts 'Example: ruby jira_clone.rb RM-1108 "Some New Title" true'
	puts '         will clone RM-1108 with all subtickets'
	puts ''
	exit	
end

def clone_issue(ticket_data)
  uri = URI.parse($jira_url + "/" + $jira_path)
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
  else
    raise StandardError, "\n-----\nFailed to clone issue " + 
      issue + "\n"+"Response :\n" + response.body + "\n-----"
  end
  exit -1
end

def fake_clone(ticket_data)
    puts JSON.pretty_generate(ticket_data)
end

def get_issue(issue, uri, summary, clone_subtasks, parent=nil)
  return_val = parent.nil?
    
  http = Net::HTTP.new(uri.host, uri.port)
  http.use_ssl = true
  http.verify_mode = OpenSSL::SSL::VERIFY_NONE

  request = Net::HTTP::Get.new(uri.request_uri)
  request.basic_auth("jira-rest-api", "jira`rest`4p1")
  response = http.request(request)

  #response = Net::HTTP.get_response(URI.parse(jira_url + issue + json_ext))
  if response.code =~ /20[0-9]{1}/
    data = JSON.parse(response.body)
    newdata = {}
    newfields = {}

    newdata["summary"] = summary
    if data["fields"]["description"].nil? || data["fields"]["description"] == ""
      data["fields"]["description"] = "Description Missing"
    end
    newdata["description"] = data["fields"]["description"]
    newdata["project"] = data["fields"]["project"]
    newdata["components"] = data["fields"]["components"]
    newdata["issuetype"] = data["fields"]["issuetype"]

    newfields["fields"] = newdata
    
    newdata["parent"] = parent unless parent.nil?

    parent = clone_issue(newfields)

    if (data["fields"]["subtasks"].size > 0)
      if clone_subtasks
        data["fields"]["subtasks"].each do |subtask|
          subtask_key = subtask["key"]
          subtask_summary = subtask["fields"]["summary"]
          subtask_uri = URI.parse($jira_url + "/" + $jira_path + subtask_key + $json_ext)
          get_issue(subtask_key, subtask_uri, subtask_summary, true, parent)
        end
      else
      end
    end
    if return_val
      return parent["key"]
    end
  else
    raise StandardError, "\n-----\nUnsuccessful response code " + response.code + " for issue " + issue +"\n"+"Response :\n" + response.body + "\n-----"
  end
end

parent_key = get_issue(issue, uri, summary, clone_subtasks)
puts parent_key
exit 0