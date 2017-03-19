/************************************************************************

THIS FILE IS AUTO-GENERATED. DO NOT EDIT DIRECTLY. ALL CHANGES WILL BE LOST.

    github_issues.cdto.c - Contains functions for working with github_issues.cdto types.

************************************************************************/
     
/************************************************************************
                              INCLUDES
************************************************************************/

#include <stdlib.h>
#include <string.h>
#include "github_issues.cdto.h"
     
/************************************************************************
                              PROCEDURES
************************************************************************/

static void label_array_free
    (
    label* array,
    int array_cnt
    );

static void user_array_free
    (
    user* array,
    int array_cnt
    );
     

/**************************************************
*
*    issue_free - Free issue
*
*    Cleans up all resources owned by the provided issue.
*
**************************************************/
void issue_free
    (
    issue* obj
    )
{
free( obj->url );
free( obj->title );
user_free( &obj->creator );
user_array_free( obj->assignees, obj->assignees_cnt );
label_array_free( obj->labels, obj->labels_cnt );

issue_init( obj );
}    /* issue_free()    */

/**************************************************
*
*    issue_init - Initialize a issue object
*
*    Zeros out the provided issue.
*
**************************************************/
void issue_init
    (
    issue* obj
    )
{
memset( obj, 0, sizeof( *obj ) );
}    /* issue_init()    */

/**************************************************
*
*    label_free - Free label
*
*    Cleans up all resources owned by the provided label.
*
**************************************************/
void label_free
    (
    label* obj
    )
{
free( obj->name );

label_init( obj );
}    /* label_free()    */

/**************************************************
*
*    label_init - Initialize a label object
*
*    Zeros out the provided label.
*
**************************************************/
void label_init
    (
    label* obj
    )
{
memset( obj, 0, sizeof( *obj ) );
}    /* label_init()    */

/**************************************************
*
*    user_free - Free user
*
*    Cleans up all resources owned by the provided user.
*
**************************************************/
void user_free
    (
    user* obj
    )
{
free( obj->name );
free( obj->url );

user_init( obj );
}    /* user_free()    */

/**************************************************
*
*    user_init - Initialize a user object
*
*    Zeros out the provided user.
*
**************************************************/
void user_init
    (
    user* obj
    )
{
memset( obj, 0, sizeof( *obj ) );
}    /* user_init()    */

/**************************************************
*
*    label_array_free - Free array
*
*    Cleans up all resources owned by the array and its elements.
*
**************************************************/
static void label_array_free
    (
    label* array,
    int array_cnt
    )
{
int i;

for( i = 0; i < array_cnt; i++ )
    {
    label_free( &array[i] );
    }

free( array );
}    /* label_array_free()    */

/**************************************************
*
*    user_array_free - Free array
*
*    Cleans up all resources owned by the array and its elements.
*
**************************************************/
static void user_array_free
    (
    user* array,
    int array_cnt
    )
{
int i;

for( i = 0; i < array_cnt; i++ )
    {
    user_free( &array[i] );
    }

free( array );
}    /* user_array_free()    */
     