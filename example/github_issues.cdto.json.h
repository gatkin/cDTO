/************************************************************************

THIS FILE IS AUTO-GENERATED. DO NOT EDIT DIRECTLY. ALL CHANGES WILL BE LOST.

    github_issues.cdto.json.h - Declares functions for parsing and serializing messages to and from JSON

************************************************************************/
     
#ifndef GITHUB_ISSUES_CDTO_JSON_H
#define GITHUB_ISSUES_CDTO_JSON_H
     
/************************************************************************
                              INCLUDES
************************************************************************/

#include "github_issues.cdto.h"
     
/************************************************************************
                              TYPES
************************************************************************/


     
/************************************************************************
                              PROCEDURES
************************************************************************/

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
     
#endif /* #ifndef GITHUB_ISSUES_CDTO_JSON_H */
     