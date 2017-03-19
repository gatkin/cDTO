/************************************************************************

THIS FILE IS AUTO-GENERATED. DO NOT EDIT DIRECTLY. ALL CHANGES WILL BE LOST.

    github_issues.cdto.h - Contains definitions for github_issues.cdto types.

************************************************************************/
     
#ifndef GITHUB_ISSUES_CDTO_H
#define GITHUB_ISSUES_CDTO_H
     
/************************************************************************
                              INCLUDES
************************************************************************/

#include <stdint.h>
     
/************************************************************************
                              TYPES
************************************************************************/

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
    int       number;
    char*     url;
    char*     title;
    user      creator;
    user*     assignees;
    int       assignees_cnt;
    label*    labels;
    int       labels_cnt;
    } issue;
     
/************************************************************************
                              PROCEDURES
************************************************************************/

void issue_free
    (
    issue* obj
    );

void issue_init
    (
    issue* obj
    );

void label_free
    (
    label* obj
    );

void label_init
    (
    label* obj
    );

void user_free
    (
    user* obj
    );

void user_init
    (
    user* obj
    );
     
#endif /* #ifndef GITHUB_ISSUES_CDTO_H */
     