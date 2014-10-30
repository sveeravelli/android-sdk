require 'rubygems'
require 'net/https'
require 'json'
# require 'io/console'

$jira_url = "https://jira.corp.ooyala.com"
$jira_path = "rest/api/latest/issue/"
issue_key = ARGV[0]
flag = ARGV[1]
username = nil
password = nil
$json_ext = ".json"
uri = URI.parse($jira_url + "/" + $jira_path + issue_key + $json_ext)

#USAGE BLOCK
if (issue_key == "--help" || issue_key == "-h")
  puts 'USAGE: ruby jira_info.rb [TICKET REF] [OPTIONS] <args>'
  puts ''
  puts '    options:'
  puts '      --name | -n [USERNAME] [PASSWORD]'
  puts '          prints the title of the ticket'
  puts '          (optional) include JIRA USERNAME and PASSWORD'
  puts '      --sub-status | -ss [STEP TITLE] [USERNAME] [PASSWORD]'
  puts '          returns the ticket status of the sub-ticket that matches STEP_TITLE'
  puts '          (optional) include JIRA USERNAME and PASSWORD'
  puts ''
  puts 'Example: ruby jira_info.rb RM-1359 --sub-status "Cut Branch"'
  puts '         will prompt user for JIRA USERNAME and PASSWORD'
  puts '         will find the child of RM-1359 with the Summary matching title "Cut Branch" '
  puts '         will return the ticket\'s status (or state)'
  puts '         e.g. "Closed" '
  puts ''
  puts 'Example: ruby jira_info.rb RM-1359 --name johndoe password'
  puts '         will find and print the title of ticket RM-1359 '
  puts ''
  puts 'See README.md for more information'
  puts ''
  exit 0
end

def get_auth_credentials()
  print "Enter your JIRA credentials."
  print "Username: "
  user = STDIN.noecho(&:gets).chomp
  print "\nPassword: "
  pswd = STDIN.noecho(&:gets).chomp
  print "\n"
  return user, pswd
end

def get_title(issue_key) 

  # username = ARGV[2] if username.nil?
  # password = ARGV[3] if password.nil?
  # if (username.nil? || password.nil?)
  #   username, password = get_auth_credentials()
  # end

  uri = URI.parse($jira_url + "/" + $jira_path + issue_key )
  http = Net::HTTP.new(uri.host, uri.port)
  http.use_ssl = true
  http.verify_mode = OpenSSL::SSL::VERIFY_NONE
  
  request = Net::HTTP::Get.new(uri.request_uri)
  # request.basic_auth(username, password)
  request.basic_auth("jira-rest-api", "jira`rest`4p1")
  response = http.request(request)
  if response.code =~ /20[0-9]{1}/
    result = JSON.parse(response.body)
    puts result["fields"]["summary"]
  else
    raise StandardError, "\n-----\nUnable to retrieve status for issue " +
      issue_key + "\n" + "Response :\n" + response.body + "\n-----"
  end
  exit -1   #ERRORS OUT
end



def get_current_status(issue_key, child_key)

  # username = ARGV[3] if username.nil?
  # password = ARGV[4] if password.nil?
  # if (username.nil? || password.nil?)
  #   username, password = get_auth_credentials()
  # end

  uri = URI.parse($jira_url + "/" + $jira_path + issue_key )
  http = Net::HTTP.new(uri.host, uri.port)
  http.use_ssl = true
  http.verify_mode = OpenSSL::SSL::VERIFY_NONE
  
  request = Net::HTTP::Get.new(uri.request_uri)
  # request.basic_auth(username, password)
  request.basic_auth("jira-rest-api", "jira`rest`4p1")
  response = http.request(request)
  if response.code =~ /20[0-9]{1}/
    result = JSON.parse(response.body)
    result["fields"]["subtasks"].each do |subtask|
      if subtask["key"] == child_key
        return subtask["fields"]["status"]["name"]
      end
    end
  else
    raise StandardError, "\n-----\nUnable to retrieve status for issue " +
      issue_key + "\n" + "Response :\n" + response.body + "\n-----"
  end
  exit -1   #ERRORS OUT
end


def find_child(issue_key, step)

  # username = ARGV[3] if username.nil?
  # password = ARGV[4] if password.nil?
  # if (username.nil? || password.nil?)
  #   username, password = get_auth_credentials()
  # end

  uri = URI.parse($jira_url + "/" + $jira_path + issue_key + $json_ext)
  http = Net::HTTP.new(uri.host, uri.port)
  http.use_ssl = true
  http.verify_mode = OpenSSL::SSL::VERIFY_NONE

  request = Net::HTTP::Get.new(uri.request_uri)
  # request.basic_auth(username, password)
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

def previous_steps_finished(issue_key, step)

  # username = ARGV[3] if username.nil?
  # password = ARGV[4] if password.nil?
  # if (username.nil? || password.nil?)
  #   username, password = get_auth_credentials()
  # end

  uri = URI.parse($jira_url + "/" + $jira_path + issue_key + $json_ext)
  http = Net::HTTP.new(uri.host, uri.port)
  http.use_ssl = true
  http.verify_mode = OpenSSL::SSL::VERIFY_NONE

  request = Net::HTTP::Get.new(uri.request_uri)
  # request.basic_auth(username, password)
  request.basic_auth("jira-rest-api", "jira`rest`4p1")
  response = http.request(request)

  #response = Net::HTTP.get_response(URI.parse(jira_url + issue_key + json_ext))
  if response.code =~ /20[0-9]{1}/
    data = JSON.parse(response.body)

    if (data["fields"]["subtasks"].size > 0)
      data["fields"]["subtasks"].each do |subtask|
          subtask_key = subtask["key"]
          if subtask["fields"]["summary"].include? step
            if (subtask["fields"]["status"]["name"] != "Open")
              puts "\"" + subtask["fields"]["summary"] + "\"" + " is " + subtask["fields"]["status"]["name"] + " so that it cannot be run twice." 
              return false
            end
            return true
          end
          if (subtask["fields"]["status"]["name"] != "Closed")
            puts "\"" + subtask["fields"]["summary"] + "\"" + " is not closed. Please finish it before moving on."
            return false;
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

if (flag == "--name" || flag == "-n") 
  get_title(issue_key)
elsif (flag == "--sub-status" || flag == "-ss")
  step = ARGV[2]
  child_key = find_child(issue_key, step)
  puts get_current_status(issue_key, child_key)
elsif (flag == "--previous-steps-finished" || flag == "-psf")
  step = ARGV[2]
  find_child(issue_key, step)
  puts previous_steps_finished(issue_key, step)
elsif (flag == "--sub-number" || flag == "-sn")
  step = ARGV[2]
  puts find_child(issue_key, step)
else
  raise StandardError, "\n Did not recognize flag. Run 'ruby jira_info.rb --help' for more info."
end
exit 0