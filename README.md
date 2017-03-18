# cDTO - Data Transport Objects for C [![Build Status](https://travis-ci.org/gatkin/cDTO.svg?branch=master)](https://travis-ci.org/gatkin/cDTO)

cDTO is a tool for defining data transport objects for the C language and generating the procedures necessary to transform them to and from various message interchange formats, such as XML and JSON. The goal of cDTO is to enable developers to build REST clients and REST services in C without needing to write all the boilerplate code necessary to validate and transform C data types between message formats.


## Using cDTO
Suppose we wanted to build a REST client to retrieve information about GitHub issues through the GitHub REST API. Our REST client would receive JSON data such as
```json
{
    "url": "https://api.github.com/repos/facebook/react/issues/8795",
    "number": 8795,
    "title": "Large update to tutorial.md's refactor section.",
    "user": {
        "login": "Jwan622",
        "url": "https://api.github.com/users/Jwan622"
    },
    "labels": [
        {
        "name": "CLA Signed",
        "color": "e7e7e7"
        },
        {
        "name": "GH Review: needs-revision",
        "color": "e11d21"
        }
    ],
    "assignees": []
}
```
To use cDTO to consume these JSON messages in our REST client, we would first define our messages in a message definition file, "github_issues.cdto"
```
issue {
    number Number cType=uint32_t;
    url String;
    title String;
    creator user jsonKey=user;
    assignees Array[user];
    labels Array[label];
}

user {
    name String jsonKey=login;
    url String;
}

label {
    name String;
    color String[6];
}
```
Next, this message definition file would be fed into cDTO to generate the corresponding C DTO type definitions along with the necessary clean up functions.
```C
// Header github_issues.cdto.h
typedef struct
    {
    char*    name;
    char*    url;
    } user;

typedef struct
    {
    char*    name;
    char     color[ 7 ];
    } label;

typedef struct
    {
    uint32_t    number;
    char*       url;
    char*       title;
    user        creator;
    user*       assignees;
    int         assignees_cnt;
    label*      labels;
    int         labels_cnt;
    } issue;

void label_free
  (
  label * label
  );

void user_free
  (
  user * user  
  );

void issue_free
  (
  issue * issue
  );
```

JSON deserialization and serialization procedures would be generated for the cDTO types. The serialization functions will handle validation of all JSON input strings and will return an error if the JSON does not match the expected message format.
```C
// Header github_issues.cdto.json.h

#include "github_issues.cdto.h"

int issue_json_parse
    (
    char const* json_str,
    issue* obj_out
    );

int issue_json_serialize
    (
    issue const* obj,
    char** json_out
    );

int issue_json_serialize_pretty
    (
    issue const* obj,
    char** json_out
    );

int label_json_parse
    (
    char const* json_str,
    label* obj_out
    );

int label_json_serialize
    (
    label const* obj,
    char** json_out
    );

int label_json_serialize_pretty
    (
    label const* obj,
    char** json_out
    );

int user_json_parse
    (
    char const* json_str,
    user* obj_out
    );

int user_json_serialize
    (
    user const* obj,
    char** json_out
    );

int user_json_serialize_pretty
    (
    user const* obj,
    char** json_out
    );
```
We can now process JSON data received from the GitHub API
```C
#include <stdio.h>
#include "github_issues.cdto.json.h"

int main()
{
  char const * json = "{"
    "\"number\": 8795,"
    "\"url\": \"https://api.github.com/repos/facebook/react/issues/8795\","
    "\"title\": \"Large update to tutorial.md's refactor section.\","
    "\"user\": {"
      "\"login\": \"Jwan622\","
      "\"url\": \"https://api.github.com/users/Jwan622\""
    "},"
    "\"labels\": ["
      "{"
        "\"name\": \"CLA Signed\","
        "\"color\": \"e7e7e7\""
      "},"
    "],"
    "\"assignees\": []"
  "}";

  issue issue;
  int success;

  success = issue_json_parse( json, &issue );
  if( success )
    {
    printf("Parsed issue %s\n", issue.title);
    }

  issue_free( &issue );

  return 0;
}
```

See the example directory for a complete, executable example of using the code generated from a cDTO definition to parse and serialize JSON data.
