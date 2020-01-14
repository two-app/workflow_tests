package com.two.workflow_tests;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
public class PartnerDetails {
    public final int uid;
    public final Integer pid;
    public final Integer cid;
    public final String firstName;
    public final String lastName;
}
