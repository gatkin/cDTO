/************************************************************************

THIS FILE IS AUTO-GENERATED. DO NOT EDIT DIRECTLY. ALL CHANGES WILL BE LOST.

    github_issues.cdto.json.c - Contains functions for parsing and serializing messages to and from JSON

************************************************************************/
     
/************************************************************************
                              INCLUDES
************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "cJSON.h"
#include "github_issues.cdto.json.h"
     
/************************************************************************
                              PROCEDURES
************************************************************************/

static int dynamic_string_json_parse
    (
    cJSON* json,
    char** value_out
    );

static int fixed_string_json_parse
    (
    cJSON* json,
    char* value_out,
    int max_length
    );

static int issue_json_obj_parse
    (
    cJSON* json_obj,
    issue* obj_out
    );

static int issue_json_obj_serialize
    (
    issue const* obj,
    cJSON** json_out
    );

static int label_array_json_parse
    (
    cJSON* json_array,
    label** array_out,
    int* array_cnt_out
    );

static int label_array_json_serialize
    (
    label const* obj_array,
    int obj_array_cnt,
    cJSON** json_out
    );

static int label_json_obj_parse
    (
    cJSON* json_obj,
    label* obj_out
    );

static int label_json_obj_serialize
    (
    label const* obj,
    cJSON** json_out
    );

static int number_json_parse
    (
    cJSON* json,
    double* value_out
    );

static int user_array_json_parse
    (
    cJSON* json_array,
    user** array_out,
    int* array_cnt_out
    );

static int user_array_json_serialize
    (
    user const* obj_array,
    int obj_array_cnt,
    cJSON** json_out
    );

static int user_json_obj_parse
    (
    cJSON* json_obj,
    user* obj_out
    );

static int user_json_obj_serialize
    (
    user const* obj,
    cJSON** json_out
    );
     

/**************************************************
*
*    issue_json_parse - Parse a issue
*
*    Parses the provided JSON string into a issue. The caller must call issue_free on obj_out.
*
**************************************************/
int issue_json_parse
    (
    char const* json_str,
    issue* obj_out
    )
{
int success;
cJSON* json_root;

issue_init( obj_out );

json_root = cJSON_Parse( json_str );
success = ( NULL != json_root );

if( success )
    {
    success = issue_json_obj_parse( json_root, obj_out );
    }

// Reset the output on error
if( !success )
    {
    issue_free( obj_out );
    }

cJSON_Delete( json_root );

return success;
}    /* issue_json_parse()    */

/**************************************************
*
*    issue_json_serialize - Serialize a issue to JSON
*
*    Serializes a issue to an unformatted JSON string. The caller must free json_out.
*
**************************************************/
int issue_json_serialize
    (
    issue const* obj,
    char** json_out
    )
{
int success;
cJSON* json_root;

json_root = NULL;
*json_out = NULL;

success = issue_json_obj_serialize( obj, &json_root );

if( success )
    {
    *json_out = cJSON_PrintUnformatted( json_root );
    success = ( NULL != *json_out );
    }

cJSON_Delete( json_root );

return success;
}    /* issue_json_serialize()    */

/**************************************************
*
*    issue_json_serialize_pretty - Pretty print a issue to JSON
*
*    Serializes a issue to a formatted JSON string. The caller must free json_out.
*
**************************************************/
int issue_json_serialize_pretty
    (
    issue const* obj,
    char** json_out
    )
{
int success;
cJSON* json_root;

json_root = NULL;
*json_out = NULL;

success = issue_json_obj_serialize( obj, &json_root );

if( success )
    {
    *json_out = cJSON_Print( json_root );
    success = ( NULL != *json_out );
    }

cJSON_Delete( json_root );

return success;
}    /* issue_json_serialize_pretty()    */

/**************************************************
*
*    label_json_parse - Parse a label
*
*    Parses the provided JSON string into a label. The caller must call label_free on obj_out.
*
**************************************************/
int label_json_parse
    (
    char const* json_str,
    label* obj_out
    )
{
int success;
cJSON* json_root;

label_init( obj_out );

json_root = cJSON_Parse( json_str );
success = ( NULL != json_root );

if( success )
    {
    success = label_json_obj_parse( json_root, obj_out );
    }

// Reset the output on error
if( !success )
    {
    label_free( obj_out );
    }

cJSON_Delete( json_root );

return success;
}    /* label_json_parse()    */

/**************************************************
*
*    label_json_serialize - Serialize a label to JSON
*
*    Serializes a label to an unformatted JSON string. The caller must free json_out.
*
**************************************************/
int label_json_serialize
    (
    label const* obj,
    char** json_out
    )
{
int success;
cJSON* json_root;

json_root = NULL;
*json_out = NULL;

success = label_json_obj_serialize( obj, &json_root );

if( success )
    {
    *json_out = cJSON_PrintUnformatted( json_root );
    success = ( NULL != *json_out );
    }

cJSON_Delete( json_root );

return success;
}    /* label_json_serialize()    */

/**************************************************
*
*    label_json_serialize_pretty - Pretty print a label to JSON
*
*    Serializes a label to a formatted JSON string. The caller must free json_out.
*
**************************************************/
int label_json_serialize_pretty
    (
    label const* obj,
    char** json_out
    )
{
int success;
cJSON* json_root;

json_root = NULL;
*json_out = NULL;

success = label_json_obj_serialize( obj, &json_root );

if( success )
    {
    *json_out = cJSON_Print( json_root );
    success = ( NULL != *json_out );
    }

cJSON_Delete( json_root );

return success;
}    /* label_json_serialize_pretty()    */

/**************************************************
*
*    user_json_parse - Parse a user
*
*    Parses the provided JSON string into a user. The caller must call user_free on obj_out.
*
**************************************************/
int user_json_parse
    (
    char const* json_str,
    user* obj_out
    )
{
int success;
cJSON* json_root;

user_init( obj_out );

json_root = cJSON_Parse( json_str );
success = ( NULL != json_root );

if( success )
    {
    success = user_json_obj_parse( json_root, obj_out );
    }

// Reset the output on error
if( !success )
    {
    user_free( obj_out );
    }

cJSON_Delete( json_root );

return success;
}    /* user_json_parse()    */

/**************************************************
*
*    user_json_serialize - Serialize a user to JSON
*
*    Serializes a user to an unformatted JSON string. The caller must free json_out.
*
**************************************************/
int user_json_serialize
    (
    user const* obj,
    char** json_out
    )
{
int success;
cJSON* json_root;

json_root = NULL;
*json_out = NULL;

success = user_json_obj_serialize( obj, &json_root );

if( success )
    {
    *json_out = cJSON_PrintUnformatted( json_root );
    success = ( NULL != *json_out );
    }

cJSON_Delete( json_root );

return success;
}    /* user_json_serialize()    */

/**************************************************
*
*    user_json_serialize_pretty - Pretty print a user to JSON
*
*    Serializes a user to a formatted JSON string. The caller must free json_out.
*
**************************************************/
int user_json_serialize_pretty
    (
    user const* obj,
    char** json_out
    )
{
int success;
cJSON* json_root;

json_root = NULL;
*json_out = NULL;

success = user_json_obj_serialize( obj, &json_root );

if( success )
    {
    *json_out = cJSON_Print( json_root );
    success = ( NULL != *json_out );
    }

cJSON_Delete( json_root );

return success;
}    /* user_json_serialize_pretty()    */

/**************************************************
*
*    dynamic_string_json_parse - Parses the given JSON object as a dynamic string. Returns 1 if the parse was successful, 0 otherwise. The caller must free value_out.
*
*    Parse dynamic JSON string
*
**************************************************/
static int dynamic_string_json_parse
    (
    cJSON* json,
    char** value_out
    )
{
int success;

*value_out = NULL;

success = ( cJSON_String == json->type );

if( success )
    {
    *value_out = strdup( json->valuestring );
    success = ( NULL != *value_out );
    }

return success;
}    /* dynamic_string_json_parse()    */

/**************************************************
*
*    fixed_string_json_parse - Parses the given JSON object as a fixed-length string. Returns 1 if the parse was successful, 0 otherwise
*
*    Parse fixed-length JSON string
*
**************************************************/
static int fixed_string_json_parse
    (
    cJSON* json,
    char* value_out,
    int max_length
    )
{
int success;
int chars_printed;

success = ( cJSON_String == json->type );

if( success )
    {
    chars_printed = snprintf( value_out, max_length, "%s", json->valuestring );
    success = ( chars_printed < max_length );
    }

return success;
}    /* fixed_string_json_parse()    */

/**************************************************
*
*    issue_json_obj_parse - Parse issue JSON object
*
*    Parses the given JSON object as a issue.
*
**************************************************/
static int issue_json_obj_parse
    (
    cJSON* json_obj,
    issue* obj_out
    )
{
int success;
cJSON* json_item;

success = 1;
issue_init( obj_out );

if( success )
    {
    json_item = cJSON_GetObjectItem( json_obj, "number" );
    success = ( NULL != json_item ) && ( number_json_parse( json_item, (double*)&obj_out->number ) );
    }

if( success )
    {
    json_item = cJSON_GetObjectItem( json_obj, "url" );
    success = ( NULL != json_item ) && ( dynamic_string_json_parse( json_item, &obj_out->url ) );
    }

if( success )
    {
    json_item = cJSON_GetObjectItem( json_obj, "title" );
    success = ( NULL != json_item ) && ( dynamic_string_json_parse( json_item, &obj_out->title ) );
    }

if( success )
    {
    json_item = cJSON_GetObjectItem( json_obj, "user" );
    success = ( NULL != json_item ) && ( user_json_obj_parse( json_item, &obj_out->creator ) );
    }

if( success )
    {
    json_item = cJSON_GetObjectItem( json_obj, "assignees" );
    success = ( NULL != json_item ) && ( user_array_json_parse( json_item, &obj_out->assignees, &obj_out->assignees_cnt ) );
    }

if( success )
    {
    json_item = cJSON_GetObjectItem( json_obj, "labels" );
    success = ( NULL != json_item ) && ( label_array_json_parse( json_item, &obj_out->labels, &obj_out->labels_cnt ) );
    }

// Reset the output on error
if( !success )
    {
    issue_free( obj_out );
    }

return success;
}    /* issue_json_obj_parse()    */

/**************************************************
*
*    issue_json_obj_serialize - Serialize a issue to JSON
*
*    Serializes the provided issue to a cJSON object. The caller must clean up json_out.
*
**************************************************/
static int issue_json_obj_serialize
    (
    issue const* obj,
    cJSON** json_out
    )
{
int success;
cJSON* json_root;
cJSON* json_item;

*json_out = NULL;

json_root = cJSON_CreateObject();
success = ( NULL != json_root );

if( success )
    {
    json_item = cJSON_CreateNumber( obj->number );
    success = ( NULL != json_item );
    }

if( success )
    {
    cJSON_AddItemToObject( json_root, "number", json_item );
    }

if( success )
    {
    json_item = cJSON_CreateString( obj->url );
    success = ( NULL != json_item );
    }

if( success )
    {
    cJSON_AddItemToObject( json_root, "url", json_item );
    }

if( success )
    {
    json_item = cJSON_CreateString( obj->title );
    success = ( NULL != json_item );
    }

if( success )
    {
    cJSON_AddItemToObject( json_root, "title", json_item );
    }

if( success )
    {
    success = user_json_obj_serialize( &obj->creator, &json_item );
    }

if( success )
    {
    cJSON_AddItemToObject( json_root, "user", json_item );
    }

if( success )
    {
    success = user_array_json_serialize( obj->assignees, obj->assignees_cnt, &json_item );
    }

if( success )
    {
    cJSON_AddItemToObject( json_root, "assignees", json_item );
    }

if( success )
    {
    success = label_array_json_serialize( obj->labels, obj->labels_cnt, &json_item );
    }

if( success )
    {
    cJSON_AddItemToObject( json_root, "labels", json_item );
    }

// Set the output or clean up on error
if( success )
    {
    *json_out = json_root;
    }
else
    {
    cJSON_Delete( json_root );
    }

return success;
}    /* issue_json_obj_serialize()    */

/**************************************************
*
*    label_array_json_parse - Parse JSON array
*
*    Parses the given JSON object as an array. Returns 1 if the parse was successful, 0 otherwise. The caller must free the parsed array
*
**************************************************/
static int label_array_json_parse
    (
    cJSON* json_array,
    label** array_out,
    int* array_cnt_out
    )
{
int success;
label* array;
int array_cnt;
int i;
cJSON* array_item;

array = NULL;
array_cnt = 0;

success = ( cJSON_Array == json_array->type );

// Allocate room to hold all items. Initialize the array's memory to
// all zeros so it is safe to free the array if an error occurs in the
// middle of parsing.
if( success )
    {
    array_cnt = cJSON_GetArraySize( json_array );
    if( array_cnt > 0 )
        {
        array = calloc( array_cnt, sizeof( *array ) );
        success = ( NULL != array );

        // Reset the array count if we failed to allocate the array
        array_cnt = success ? array_cnt : 0;
        }
    }

for( i = 0; success && ( i < array_cnt ); i++ )
    {
    array_item = cJSON_GetArrayItem( json_array, i );
    success = label_json_obj_parse( array_item, &array[i] );
    }

*array_out = array;
*array_cnt_out = array_cnt;

return success;
}    /* label_array_json_parse()    */

/**************************************************
*
*    label_array_json_serialize - Serialize an array of label objects
*
*    Serializes an array of label objects to a cJSON array object. The caller must clean up json_out.
*
**************************************************/
static int label_array_json_serialize
    (
    label const* obj_array,
    int obj_array_cnt,
    cJSON** json_out
    )
{
int success;
cJSON* json_array;
cJSON* array_item;
int i;

*json_out = NULL;

json_array = cJSON_CreateArray();
success = ( NULL != json_array );

for( i = 0; ( success ) && ( i < obj_array_cnt ); i++ )
    {
    success = label_json_obj_serialize( &obj_array[i], &array_item );

    if( success )
        {
        cJSON_AddItemToArray( json_array, array_item );
        }
     }

// Set the output or clean up on error
if( success )
    {
    *json_out = json_array;
    }
else
    {
    cJSON_Delete( json_array );
    }

return success;
}    /* label_array_json_serialize()    */

/**************************************************
*
*    label_json_obj_parse - Parse label JSON object
*
*    Parses the given JSON object as a label.
*
**************************************************/
static int label_json_obj_parse
    (
    cJSON* json_obj,
    label* obj_out
    )
{
int success;
cJSON* json_item;

success = 1;
label_init( obj_out );

if( success )
    {
    json_item = cJSON_GetObjectItem( json_obj, "name" );
    success = ( NULL != json_item ) && ( dynamic_string_json_parse( json_item, &obj_out->name ) );
    }

if( success )
    {
    json_item = cJSON_GetObjectItem( json_obj, "color" );
    success = ( NULL != json_item ) && ( fixed_string_json_parse( json_item, obj_out->color, sizeof( obj_out->color ) ) );
    }

// Reset the output on error
if( !success )
    {
    label_free( obj_out );
    }

return success;
}    /* label_json_obj_parse()    */

/**************************************************
*
*    label_json_obj_serialize - Serialize a label to JSON
*
*    Serializes the provided label to a cJSON object. The caller must clean up json_out.
*
**************************************************/
static int label_json_obj_serialize
    (
    label const* obj,
    cJSON** json_out
    )
{
int success;
cJSON* json_root;
cJSON* json_item;

*json_out = NULL;

json_root = cJSON_CreateObject();
success = ( NULL != json_root );

if( success )
    {
    json_item = cJSON_CreateString( obj->name );
    success = ( NULL != json_item );
    }

if( success )
    {
    cJSON_AddItemToObject( json_root, "name", json_item );
    }

if( success )
    {
    json_item = cJSON_CreateString( obj->color );
    success = ( NULL != json_item );
    }

if( success )
    {
    cJSON_AddItemToObject( json_root, "color", json_item );
    }

// Set the output or clean up on error
if( success )
    {
    *json_out = json_root;
    }
else
    {
    cJSON_Delete( json_root );
    }

return success;
}    /* label_json_obj_serialize()    */

/**************************************************
*
*    number_json_parse - Parses the given JSON object as a number. Returns 1 if the parse was successful, 0 otherwise.
*
*    Parse JSON number
*
**************************************************/
static int number_json_parse
    (
    cJSON* json,
    double* value_out
    )
{
int success;

if( cJSON_Number == json->type )
    {
    *value_out = json->valuedouble;
    success = 1;
    }
else
    {
    success = 0;
    }

return success;
}    /* number_json_parse()    */

/**************************************************
*
*    user_array_json_parse - Parse JSON array
*
*    Parses the given JSON object as an array. Returns 1 if the parse was successful, 0 otherwise. The caller must free the parsed array
*
**************************************************/
static int user_array_json_parse
    (
    cJSON* json_array,
    user** array_out,
    int* array_cnt_out
    )
{
int success;
user* array;
int array_cnt;
int i;
cJSON* array_item;

array = NULL;
array_cnt = 0;

success = ( cJSON_Array == json_array->type );

// Allocate room to hold all items. Initialize the array's memory to
// all zeros so it is safe to free the array if an error occurs in the
// middle of parsing.
if( success )
    {
    array_cnt = cJSON_GetArraySize( json_array );
    if( array_cnt > 0 )
        {
        array = calloc( array_cnt, sizeof( *array ) );
        success = ( NULL != array );

        // Reset the array count if we failed to allocate the array
        array_cnt = success ? array_cnt : 0;
        }
    }

for( i = 0; success && ( i < array_cnt ); i++ )
    {
    array_item = cJSON_GetArrayItem( json_array, i );
    success = user_json_obj_parse( array_item, &array[i] );
    }

*array_out = array;
*array_cnt_out = array_cnt;

return success;
}    /* user_array_json_parse()    */

/**************************************************
*
*    user_array_json_serialize - Serialize an array of user objects
*
*    Serializes an array of user objects to a cJSON array object. The caller must clean up json_out.
*
**************************************************/
static int user_array_json_serialize
    (
    user const* obj_array,
    int obj_array_cnt,
    cJSON** json_out
    )
{
int success;
cJSON* json_array;
cJSON* array_item;
int i;

*json_out = NULL;

json_array = cJSON_CreateArray();
success = ( NULL != json_array );

for( i = 0; ( success ) && ( i < obj_array_cnt ); i++ )
    {
    success = user_json_obj_serialize( &obj_array[i], &array_item );

    if( success )
        {
        cJSON_AddItemToArray( json_array, array_item );
        }
     }

// Set the output or clean up on error
if( success )
    {
    *json_out = json_array;
    }
else
    {
    cJSON_Delete( json_array );
    }

return success;
}    /* user_array_json_serialize()    */

/**************************************************
*
*    user_json_obj_parse - Parse user JSON object
*
*    Parses the given JSON object as a user.
*
**************************************************/
static int user_json_obj_parse
    (
    cJSON* json_obj,
    user* obj_out
    )
{
int success;
cJSON* json_item;

success = 1;
user_init( obj_out );

if( success )
    {
    json_item = cJSON_GetObjectItem( json_obj, "login" );
    success = ( NULL != json_item ) && ( dynamic_string_json_parse( json_item, &obj_out->name ) );
    }

if( success )
    {
    json_item = cJSON_GetObjectItem( json_obj, "url" );
    success = ( NULL != json_item ) && ( dynamic_string_json_parse( json_item, &obj_out->url ) );
    }

// Reset the output on error
if( !success )
    {
    user_free( obj_out );
    }

return success;
}    /* user_json_obj_parse()    */

/**************************************************
*
*    user_json_obj_serialize - Serialize a user to JSON
*
*    Serializes the provided user to a cJSON object. The caller must clean up json_out.
*
**************************************************/
static int user_json_obj_serialize
    (
    user const* obj,
    cJSON** json_out
    )
{
int success;
cJSON* json_root;
cJSON* json_item;

*json_out = NULL;

json_root = cJSON_CreateObject();
success = ( NULL != json_root );

if( success )
    {
    json_item = cJSON_CreateString( obj->name );
    success = ( NULL != json_item );
    }

if( success )
    {
    cJSON_AddItemToObject( json_root, "login", json_item );
    }

if( success )
    {
    json_item = cJSON_CreateString( obj->url );
    success = ( NULL != json_item );
    }

if( success )
    {
    cJSON_AddItemToObject( json_root, "url", json_item );
    }

// Set the output or clean up on error
if( success )
    {
    *json_out = json_root;
    }
else
    {
    cJSON_Delete( json_root );
    }

return success;
}    /* user_json_obj_serialize()    */
     