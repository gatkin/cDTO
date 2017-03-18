#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>

#include "github_issues.cdto.json.h"

#define JSON_FILE ( "issues.json" )

static int json_file_read
    (
    char const * json_file_path,
    char **      json_out
    );

static void parse_example
    (
    void
    );

static void serialize_example
    (
    void
    );


int main
    (
    void
    )
{
parse_example();
serialize_example();
return 0;
}


static void parse_example
    (
    void
    )
{
int success;
char * json_data = NULL;
issue parsed_issue;

issue_init( &parsed_issue );

success = json_file_read( JSON_FILE, &json_data );

if( success )
    {
    success = issue_json_parse( json_data, &parsed_issue );
    }

if( success )
    {
    printf( "Parsed issue %s\n", parsed_issue.title );
    }

issue_free( &parsed_issue );
free( json_data );
}


static void serialize_example
    (
    void
    )
{
user creator = 
    {
        "user1",
        "http://github.com/user/user1",
    };

label labels[] = 
    {
        {
            "issue-label",
            "e7e7e7",
        },
        {
            "another-issue-label",
            "ffffff",
        },
    };
int labels_cnt = 2;

user * assignees = NULL;
int assignees_cnt = 0;

issue issue =
    {
        1234,
        "http://github.com/issue/1234",
        "Example issue",
        creator,
        assignees,
        assignees_cnt,
        labels,
        labels_cnt,
    };

int success;
char * serialized_issue = NULL;

success = issue_json_serialize_pretty( &issue, &serialized_issue );

if( success )
    {
    printf( "%s\n", serialized_issue );
    }

free( serialized_issue );
}


static int json_file_read
    (
    char const * json_file_path,
    char **      json_out
    )
{
int success;
struct stat file_stats;
FILE * json_file;
size_t bytes_read;

json_file = NULL;
*json_out = NULL;

success = ( 0 == stat( json_file_path, &file_stats ) );

if( success )
    {
    json_file = fopen( JSON_FILE, "r" );
    success = ( NULL != json_file );
    }

if( success )
    {
    *json_out = calloc( file_stats.st_size + 1, 1 );
    success = ( NULL != *json_out );
    }

if( success )
    {
    bytes_read = fread( *json_out, 1, file_stats.st_size, json_file );
    success = ( bytes_read == file_stats.st_size );
    }

if( !success )
    {
    free( *json_out );
    *json_out = NULL;
    }

fclose( json_file );

return success;
}
