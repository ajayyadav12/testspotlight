package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.Submission;
import com.ge.finance.spotlight.models.SubmissionStep;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class SubmissionStepRepositoryJpaTest {

    @Autowired private SubmissionRepository submissionRepository;
    @Autowired private SubmissionStepRepository submissionStepRepository;

    @Test
    public void testFindBySubmissionOrderByStartTimeAsc() {
        Calendar calendar = Calendar.getInstance();
        Submission submission = new Submission();
        submission = submissionRepository.save(submission);
        SubmissionStep firstSubmissionStep = new SubmissionStep();
        firstSubmissionStep.setSubmissionId(submission.getId());
        firstSubmissionStep.setStartTime(calendar.getTime());
        firstSubmissionStep = submissionStepRepository.save(firstSubmissionStep);
        SubmissionStep secondSubmissionStep = new SubmissionStep();
        secondSubmissionStep.setSubmissionId(submission.getId());
        calendar.add(Calendar.MINUTE, -1);
        secondSubmissionStep.setStartTime(calendar.getTime());
        secondSubmissionStep = submissionStepRepository.save(secondSubmissionStep);
        List<SubmissionStep> submissionSteps = submissionStepRepository.findBySubmissionIdOrderByStartTimeAsc(submission.getId());
        assertEquals(2, submissionSteps.size());
        assertEquals(secondSubmissionStep.getId(), submissionSteps.get(0).getId());
        assertEquals(firstSubmissionStep.getId(), submissionSteps.get(1).getId());
    }

}
