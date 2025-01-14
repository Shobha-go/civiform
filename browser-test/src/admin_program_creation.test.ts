import { startSession, loginAsAdmin, AdminQuestions, AdminPrograms, endSession } from './support'

describe('Create program with enumerator and repeated questions', () => {
  it('create program with enumerator and repeated questions', async () => {
    const { browser, page } = await startSession();
    page.setDefaultTimeout(4000);

    await loginAsAdmin(page);
    const adminQuestions = new AdminQuestions(page);
    const adminPrograms = new AdminPrograms(page);

    await adminQuestions.addAddressQuestion('apc-address');
    await adminQuestions.addNameQuestion('apc-name');
    await adminQuestions.addTextQuestion('apc-text');
    await adminQuestions.addEnumeratorQuestion('apc-enumerator');
    await adminQuestions.addTextQuestion('apc-repeated', 'description', '\$this text', '\$this helptext', 'apc-enumerator');

    const programName = 'apc program';
    await adminPrograms.addProgram(programName);
    await adminPrograms.editProgramBlock(programName, 'apc program description');

    // All non-repeated questions should be available in the question bank
    expect(await page.innerText('id=question-bank-questions')).toContain('apc-address');
    expect(await page.innerText('id=question-bank-questions')).toContain('apc-name');
    expect(await page.innerText('id=question-bank-questions')).toContain('apc-text');
    expect(await page.innerText('id=question-bank-questions')).toContain('apc-enumerator');
    expect(await page.innerText('id=question-bank-questions')).not.toContain('apc-repeated');

    // Add a non-enumerator question and the enumerator option should go away
    await page.click('button:text("apc-name")');
    expect(await page.innerText('id=question-bank-questions')).not.toContain('apc-enumerator');
    expect(await page.innerText('id=question-bank-questions')).not.toContain('apc-repeated');

    // Remove the non-enumerator question and add a enumerator question. All options should go away..
    await page.click('button:text("apc-name")');
    await page.click('button:text("apc-enumerator")');
    expect(await page.innerText('id=question-bank-questions')).toBe('Question bank');

    // Create a repeated block. The repeated question should be the only option.
    await page.click('#create-repeated-block-button');
    expect(await page.innerText('id=question-bank-questions')).toContain('apc-repeated');

    await endSession(browser);
  })
})
